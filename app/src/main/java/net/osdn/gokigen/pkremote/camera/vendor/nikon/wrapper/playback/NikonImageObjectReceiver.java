package net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.playback;

import android.util.Log;
import android.util.SparseArray;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.NikonInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;

import java.util.ArrayList;
import java.util.List;

import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_STORAGE_ID;

public class NikonImageObjectReceiver implements IPtpIpCommandCallback, NikonStorageContentHolder.ImageObjectReceivedCallback
{
    private final String TAG = toString();
    private final NikonInterfaceProvider provider;
    private final int delayMs;
    private boolean isDumpLog = false;
    private ICameraContentListCallback callback = null;
    private SparseArray<NikonStorageContentHolder> storageIdList;

    NikonImageObjectReceiver(NikonInterfaceProvider provider, int delayMs)
    {
        this.provider = provider;
        this.delayMs = delayMs;
        this.storageIdList = new SparseArray<>();
    }

    private void parseStorageId(byte[] rx_body)
    {
        try
        {
            storageIdList.clear();
            //SimpleLogDumper.dump_bytes(" [GetStorageIds] ", rx_body);

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
                storageIdList.append(storageId, new NikonStorageContentHolder(provider, storageId, this, delayMs));
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
            if (id == GET_STORAGE_ID)
            {
                // ストレージID一覧を解析する
                parseStorageId(rx_body);
                for(int index = 0; index < storageIdList.size(); index++)
                {
                    int key = storageIdList.keyAt(index);
                    NikonStorageContentHolder contentHolder = storageIdList.get(key);
                    contentHolder.getContents();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

    NikonImageContentInfo getContentObject(String fileName)
    {
        for(int index = 0; index < storageIdList.size(); index++)
        {
            int key = storageIdList.keyAt(index);
            NikonStorageContentHolder contentHolder = storageIdList.get(key);
            SparseArray<NikonImageContentInfo> objectArray = contentHolder.getObjectIdList();
            int objectId = Integer.parseInt(fileName.substring(fileName.indexOf("/") + 3, fileName.indexOf(".")), 16);
            NikonImageContentInfo content = objectArray.get(objectId);
            if (content != null)
            {
                return (content);
            }
        }
        return (null);
    }

    NikonImageContentInfo getImageContent(int objectId)
    {
        for(int index = 0; index < storageIdList.size(); index++)
        {
            int key = storageIdList.keyAt(index);
            NikonStorageContentHolder contentHolder = storageIdList.get(key);
            SparseArray<NikonImageContentInfo> objectArray = contentHolder.getObjectIdList();
            NikonImageContentInfo content = objectArray.get(objectId);
            if (content != null)
            {
                return (content);
            }
        }
        return (null);
    }


    void getCameraContents(ICameraContentListCallback callback)
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
                publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_ID, delayMs, isDumpLog, 0, 0x1004, 0, 0, 0, 0, 0));  // GetStorageIDs
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
        if (isDumpLog)
        {
            Log.v(TAG, " ----- STORAGE ID : " + storageId + " -----");
        }
        List<ICameraContent> objectList = new ArrayList<>();
        for(int index = 0; index < storageIdList.size(); index++)
        {
            int key = storageIdList.keyAt(index);
            NikonStorageContentHolder contentHolder = storageIdList.get(key);
            boolean receivedAll = contentHolder.isObjectIdReceived();
            if (!receivedAll)
            {
                if (isDumpLog)
                {
                    Log.v(TAG, " checking : " + key);  // まだストレージ情報がすべて取得できていない
                }
                return;
            }
            SparseArray<NikonImageContentInfo> objectArray = contentHolder.getObjectIdList();
            for (int objectIndex = 0; objectIndex < objectArray.size(); objectIndex++)
            {
                objectList.add(objectArray.valueAt(objectIndex));
            }
        }

        //  すべてのStorageで Object Id の取得が終わった！
        Log.v(TAG," ----- RECEIVED ALL IMAGE OBJECT ID  count : " + objectList.size() + " -----");
        callback.onCompleted(objectList);
    }

    @Override
    public void onError(Exception e)
    {
        Log.v(TAG, "onError : " + e.getLocalizedMessage());
    }
}
