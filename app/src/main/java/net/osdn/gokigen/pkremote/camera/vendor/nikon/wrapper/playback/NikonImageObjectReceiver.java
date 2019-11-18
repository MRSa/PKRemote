package net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.playback;

import android.util.Log;
import android.util.SparseArray;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.utils.SimpleLogDumper;
import net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.NikonInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback.PtpIpImageContentInfo;

import java.util.ArrayList;
import java.util.List;

import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_OBJECT_INFO_EX_2;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_OBJECT_INFO_EX_3;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_STORAGE_ID;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_STORAGE_INFO;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_OBJECT_INFO_EX;

public class NikonImageObjectReceiver implements IPtpIpCommandCallback, NikonStorageContentHolder.ImageObjectReceivedCallback
{
    private final String TAG = toString();
    private final NikonInterfaceProvider provider;
    private boolean isDumpLog = true;
    private List<ICameraContent> imageObjectList;
    private List<PtpIpImageContentInfo> ptpIpImageObjectList;
    private ICameraContentListCallback callback = null;
    private int subDirectoriesCount = -1;
    private int receivedSubDirectoriesCount = -1;
    private SparseArray<NikonStorageContentHolder> storageIdList;

    NikonImageObjectReceiver(NikonInterfaceProvider provider)
    {
        this.provider = provider;
        this.imageObjectList = new ArrayList<>();
        this.ptpIpImageObjectList = new ArrayList<>();

        this.storageIdList = new SparseArray<>();
    }

    private void parseStorageId(byte[] rx_body)
    {
        try
        {
            storageIdList.clear();
            SimpleLogDumper.dump_bytes(" [GetStorageIds] ", rx_body);

            int checkBytes = rx_body[0];
            //int dataLength = rx_body[checkBytes];

            int nofStorages = rx_body[checkBytes + 12];
            int readPosition = checkBytes + 12 + 4;
            Log.v(TAG, " NOF STORAGES : " + nofStorages);
            for (int index = 0; index < nofStorages; index++)
            {
                int storageId =  ((int) rx_body[readPosition]) +
                                 ((int) rx_body[readPosition + 1] << 8) +
                                 ((int) rx_body[readPosition + 2] << 16) +
                                 ((int) rx_body[readPosition + 3] << 24);
                storageIdList.append(storageId, new NikonStorageContentHolder(provider, storageId, this));
                readPosition = readPosition + 4;
            }
            Log.v(TAG, " NOF STORAGE IDs : " + storageIdList.size() + " ");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            //SimpleLogDumper.dump_bytes(" [RX] ", rx_body);
            switch (id)
            {
                case GET_STORAGE_ID:
                    // ストレージID一覧を解析する
                    parseStorageId(rx_body);
                    for(int index = 0; index < storageIdList.size(); index++)
                    {
                        int key = storageIdList.keyAt(index);
                        NikonStorageContentHolder contentHolder = storageIdList.get(key);
                        contentHolder.getContents();
                    }
                    subDirectoriesCount = -1;  // ここから画像取得シーケンスに入るので、、、
                    break;

                case GET_STORAGE_INFO:
                    // TODO: (要検討) ストレージの情報を取得しているが、本当に使わなくてもよい？
                    publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_OBJECT_INFO_EX, isDumpLog, 0, 0x9109, 12, 0x00010001, 0xffffffff, 0x00200000));
                    break;

                case GET_OBJECT_INFO_EX:
                    List<PtpIpImageContentInfo> directries =  parseContentSubdirectories(rx_body, 32);
                    {
                        // サブディレクトリの情報を拾う
                        for (PtpIpImageContentInfo contentInfo : directries)
                        {
                            publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_OBJECT_INFO_EX_2, isDumpLog, 0, 0x9109, 12, 0x00010001, contentInfo.getId(), 0x00200000));
                        }
                    }
                    break;

                case GET_OBJECT_INFO_EX_2:
                    List<PtpIpImageContentInfo> subDirectries =  parseContentSubdirectories(rx_body, 32);
                    {
                        // 画像の情報を拾う
                        for (PtpIpImageContentInfo contentInfo : subDirectries)
                        {
                            publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_OBJECT_INFO_EX_3, isDumpLog, 0, 0x9109, 12, 0x00010001, contentInfo.getId(), 0x00200000));
                        }
                        subDirectoriesCount = subDirectries.size();
                        receivedSubDirectoriesCount = 0;
                        if (subDirectoriesCount <= 0)
                        {
                            // カメラの画像コンテンツが見つからなかった（サブディレクトリがなかった）...ここで画像解析終了の報告をする
                            callback.onCompleted(imageObjectList);
                        }
                    }
                    break;

                case GET_OBJECT_INFO_EX_3:
                    if (isDumpLog)
                    {
                        Log.v(TAG, " --- CONTENT ---");
                    }
                    List<PtpIpImageContentInfo> objects =  parseContentSubdirectories(rx_body, 32);
                    if (objects.size() > 0)
                    {
                        imageObjectList.addAll(objects);
                        ptpIpImageObjectList.addAll(objects);
                    }
                    receivedSubDirectoriesCount++;
                    if (receivedSubDirectoriesCount >= subDirectoriesCount)
                    {
                        // 全コンテンツの受信成功
                        if(this.callback != null)
                        {
                            callback.onCompleted(imageObjectList);
                        }
                    }
                    break;

                default:
/*
                    if ((id & 0xff) == GET_STORAGE_HANDLE1)
                    {
                    }
*/
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private List<PtpIpImageContentInfo> parseContentSubdirectories(byte[] rx_body, int offset)
    {
        List<PtpIpImageContentInfo> result = new ArrayList<>();
        try
        {
            int nofObjects = (rx_body[offset] & 0xff);
            nofObjects = nofObjects + ((rx_body[offset + 1]  & 0xff) << 8);
            nofObjects = nofObjects + ((rx_body[offset + 2] & 0xff) << 16);
            nofObjects = nofObjects + ((rx_body[offset + 3] & 0xff) << 24);

            int dataIndex = offset + 4;
            while (rx_body.length > dataIndex)
            {
                int objectSize = (rx_body[dataIndex++] & 0xff);
                objectSize = objectSize + ((rx_body[dataIndex++]  & 0xff) << 8);
                objectSize = objectSize + ((rx_body[dataIndex++] & 0xff) << 16);
                objectSize = objectSize + ((rx_body[dataIndex++] & 0xff) << 24);
                objectSize = objectSize - 4;  // 抽出したレングス長分減らず

                int id = (rx_body[dataIndex] & 0xff);
                id = id + ((rx_body[dataIndex + 1]  & 0xff) << 8);
                id = id + ((rx_body[dataIndex + 2] & 0xff) << 16);
                id = id + ((rx_body[dataIndex + 3] & 0xff) << 24);

                PtpIpImageContentInfo content = new PtpIpImageContentInfo(id, "", rx_body, dataIndex, objectSize);
                result.add(content);
                dataIndex = dataIndex + objectSize;
                if (result.size() >= nofObjects)
                {
                    // オブジェクトを全部切り出した
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (result);
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
    {
        Log.v(TAG, "onReceiveProgress(" + currentBytes + "/" + totalBytes + ")");
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    PtpIpImageContentInfo getContentObject(String fileName)
    {
        for (PtpIpImageContentInfo contentInfo : ptpIpImageObjectList)
        {

            if (fileName.matches(contentInfo.getContentName()))
            {
                return (contentInfo);
            }
        }
        return (null);
    }

    public void getCameraContents(ICameraContentListCallback callback)
    {
        this.callback = null;
        try
        {
            ICameraConnection connection = provider.getPtpIpCameraConnection();
            if (connection.getConnectionStatus() != ICameraConnection.CameraConnectionStatus.CONNECTED)
            {
                Log.v(TAG, "DOES NOT CONNECT TO CAMERA.");
                return;
            }

            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            if (publisher != null)
            {
                // オブジェクト一覧をクリアする
                this.imageObjectList.clear();
                this.ptpIpImageObjectList.clear();

                publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_ID, isDumpLog, 0, 0x1004));  // GetStorageIDs
                this.callback = callback;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceived(int storageId)
    {
        Log.v(TAG, " ----- STORAGE ID : " + storageId + " -----");

        List<Integer> objectList = new ArrayList<>();
        for(int index = 0; index < storageIdList.size(); index++)
        {
            int key = storageIdList.keyAt(index);
            NikonStorageContentHolder contentHolder = storageIdList.get(key);
            boolean receivedAll = contentHolder.isObjectIdReceived();
            if (!receivedAll)
            {
                return;
            }
            objectList.addAll(contentHolder.getObjectIdList());
        }
        //  すべてのStorageで Object Id の取得が終わった！
        Log.v(TAG," ----- RECEIVED ALL IMAGE OBJECT ID  count : " + objectList.size() + "-----");
        for (int id : objectList)
        {
            Log.v(TAG, "  OBJECT ID : " + id);
        }
        Log.v(TAG, " --------------------");
    }

    @Override
    public void onError(Exception e)
    {
        Log.v(TAG, "onError : " + e.getLocalizedMessage());

    }
}
