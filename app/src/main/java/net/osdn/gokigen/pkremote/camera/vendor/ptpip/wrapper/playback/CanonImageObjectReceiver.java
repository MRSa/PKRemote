package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.util.Log;
import android.util.SparseArray;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.PtpIpInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;

import java.util.ArrayList;
import java.util.List;

import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_STORAGE_ID;

public class CanonImageObjectReceiver implements IPtpIpCommandCallback, CanonStorageContentHolder.ImageObjectReceivedCallback
{
    private final String TAG = toString();
    private final PtpIpInterfaceProvider provider;
    private final int delayMs;
    private boolean isDumpLog = false;
    private List<CanonImageContentInfo> ptpIpImageObjectList;
    private ICameraContentListCallback callback = null;
    private SparseArray<CanonStorageContentHolder> storageIdList;

    CanonImageObjectReceiver(PtpIpInterfaceProvider provider, int delayMs)
    {
        this.provider = provider;
        this.delayMs = delayMs;
        this.ptpIpImageObjectList = new ArrayList<>();
        this.storageIdList = new SparseArray<>();
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
                    CanonStorageContentHolder contentHolder = storageIdList.get(key);
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

    CanonImageContentInfo getContentObject(String fileName)
    {
        for (CanonImageContentInfo contentInfo : ptpIpImageObjectList)
        {

            if (fileName.matches(contentInfo.getContentName()))
            {
                return (contentInfo);
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
                this.ptpIpImageObjectList.clear();
                publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_ID, isDumpLog, 0, 0x9101));
                this.callback = callback;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void parseStorageId(byte[] rx_body)
    {
        try
        {
            storageIdList.clear();
            // SimpleLogDumper.dump_bytes(" [GetStorageIds] ", rx_body);
            int checkBytes = rx_body[0];
            int nofStorages = rx_body[checkBytes + 12];
            int readPosition = checkBytes + 12 + 4;

            for (int index = 0; index < nofStorages; index++)
            {
                int storageId =  ((int) rx_body[readPosition]) +
                        ((int) rx_body[readPosition + 1] << 8) +
                        ((int) rx_body[readPosition + 2] << 16) +
                        ((int) rx_body[readPosition + 3] << 24);
                storageIdList.append(storageId, new CanonStorageContentHolder(provider, storageId, this, delayMs));
                readPosition = readPosition + 4;
            }
            Log.v(TAG, " NOF STORAGE IDs : " + storageIdList.size() + " (" + nofStorages + ") ");
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
            Log.v(TAG, " ----- STORAGE ID : " + storageId + " ----- : " + storageIdList.size());
        }
        List<ICameraContent> objectList = new ArrayList<>();
        for(int index = 0; index < storageIdList.size(); index++)
        {
            int key = storageIdList.keyAt(index);
            CanonStorageContentHolder contentHolder = storageIdList.get(key);
            boolean receivedAll = contentHolder.isObjectIdReceived();
            if (!receivedAll)
            {
                if (isDumpLog)
                {
                    Log.v(TAG, " checking : " + key);  // まだストレージ情報がすべて取得できていない
                }
                return;
            }
            SparseArray<CanonImageContentInfo> objectArray = contentHolder.getObjectIdList();
            for (int objectIndex = 0; objectIndex < objectArray.size(); objectIndex++)
            {
                CanonImageContentInfo value = objectArray.valueAt(objectIndex);
                ptpIpImageObjectList.add(value);
                objectList.add(value);
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
