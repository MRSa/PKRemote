package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.PtpIpInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;

import java.util.ArrayList;
import java.util.List;

import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_OBJECT_INFO_EX;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_OBJECT_INFO_EX_2;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_OBJECT_INFO_EX_3;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_STORAGE_INFO;

public class CanonStorageContentHolder implements IPtpIpCommandCallback
{
    private final String TAG = toString();
    private final int storageId;
    private final PtpIpInterfaceProvider provider;
    private final ImageObjectReceivedCallback callback;
    private boolean isDumpLog = false;
    private int subDirectoriesCount = -1;
    private int receivedSubDirectoriesCount = -1;
    private int delayMs;
    private boolean isObjectReceived = false;
    private SparseArray<CanonImageContentInfo> imageObjectList;

    CanonStorageContentHolder(@NonNull PtpIpInterfaceProvider provider, int storageId, @NonNull ImageObjectReceivedCallback callback, int delayMs)
    {
        this.provider = provider;
        this.storageId = storageId;
        this.callback = callback;
        this.delayMs = delayMs;
        this.imageObjectList = new SparseArray<>();
    }

    void getContents()
    {
        try
        {
            isObjectReceived = false;
            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_INFO, delayMs, isDumpLog,  0, 0x9102, 4, storageId, 0 , 0, 0));  // ストレージトップのオブジェクト情報を取得する
            imageObjectList.clear();
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
            if (id == GET_STORAGE_INFO)
            {
                // トップディレクトリを取得した
                /*
                List<Integer> directoriesList = parseObjects(rx_body);
                for (int subDirectory : directoriesList)
                {
                    Log.v(TAG, "  STORAGE ID : " + storageId + "  DIRECTORY ID : " + subDirectory);
                    publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_OBJECT_INFO_EX, delayMs, isDumpLog, 0, 0x9109, 12, storageId, 0xffffffff, 0x00200000, 0));
                    //publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_HANDLE2, delayMs, isDumpLog, 0, 0x1007, 12, storageId, 0x00000000, subDirectory, 0)); //
                }
                */
                ////////  本来は、 GET_STORAGE_INFO の応答結果を解析し、 value2 の 0xffffffff を取得すべき
                publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_OBJECT_INFO_EX, delayMs, isDumpLog, 0, 0x9109, 12, storageId, 0xffffffff, 0x00200000, 0));
                return;
            }
            if (id == GET_OBJECT_INFO_EX)
            {
                List<CanonImageContentInfo> directries =  parseContentSubdirectories(rx_body, 32, false);
                {
                    // サブディレクトリの情報を拾う
                    for (CanonImageContentInfo contentInfo : directries)
                    {
                        publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_OBJECT_INFO_EX_2, delayMs, isDumpLog, 0, 0x9109, 12, storageId, contentInfo.getId(), 0x00200000, 0));
                    }
                }
            }
            if (id == GET_OBJECT_INFO_EX_2)
            {
                List<CanonImageContentInfo> subDirectries =  parseContentSubdirectories(rx_body, 32, false);
                {
                    // 画像の情報を拾う
                    for (CanonImageContentInfo contentInfo : subDirectries)
                    {
                        publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_OBJECT_INFO_EX_3, delayMs, isDumpLog, 0, 0x9109, 12, storageId, contentInfo.getId(), 0x00200000, 0));
                    }
                    subDirectoriesCount = subDirectries.size();
                    receivedSubDirectoriesCount = 0;
                    if (subDirectoriesCount <= 0)
                    {
                        // カメラの画像コンテンツが見つからなかった（サブディレクトリがなかった）...ここで画像解析終了の報告をする
                        isObjectReceived = true;
                        callback.onReceived(storageId);
                    }
                }
            }
            if (id == GET_OBJECT_INFO_EX_3)
            {
                List<CanonImageContentInfo> objects = parseContentSubdirectories(rx_body, 32, true);
                if (isDumpLog)
                {
                    Log.v(TAG, " --- CONTENT --- : " + objects.size());
                }
                receivedSubDirectoriesCount++;
                if (receivedSubDirectoriesCount >= subDirectoriesCount)
                {
                    // 全コンテンツの受信成功
                    isObjectReceived = true;
                    callback.onReceived(storageId);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callback.onError(e);
        }
    }

    private List<CanonImageContentInfo> parseContentSubdirectories(byte[] rx_body, int offset, boolean isEntry)
    {
        List<CanonImageContentInfo> result = new ArrayList<>();
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

                CanonImageContentInfo content = new CanonImageContentInfo(id, "", rx_body, dataIndex, objectSize);
                result.add(content);
                if (isEntry)
                {
                    imageObjectList.append(id, content);
                }
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

    SparseArray<CanonImageContentInfo> getObjectIdList()
    {
        return (imageObjectList);
    }

    boolean isObjectIdReceived()
    {
        return (isObjectReceived);
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
    {
        //
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    interface ImageObjectReceivedCallback
    {
        void onReceived(int storageId);
        void onError(Exception e);
    }
}
