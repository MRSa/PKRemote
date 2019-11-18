package net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.playback;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.NikonInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;

import java.util.ArrayList;
import java.util.List;

import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_STORAGE_HANDLE1;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_STORAGE_HANDLE2;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_STORAGE_HANDLE3;

public class NikonStorageContentHolder  implements IPtpIpCommandCallback
{
    private final String TAG = toString();
    private final int storageId;
    private final NikonInterfaceProvider provider;
    private final ImageObjectReceivedCallback callback;
    private boolean isDumpLog = true;
    private List<Integer> objectList;
    private int subDirectoryCount = 0;
    private int receivedDirectoryCount = 0;
    private boolean isObjectReceived = false;

    NikonStorageContentHolder(@NonNull NikonInterfaceProvider provider, int storageId, @NonNull ImageObjectReceivedCallback callback)
    {
        this.provider = provider;
        this.storageId = storageId;
        this.callback = callback;
        this.objectList = new ArrayList<>();
    }

    void getContents()
    {
        try
        {
            isObjectReceived = false;
            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_HANDLE1, isDumpLog, 0, 0x1007, 12, storageId, 0x00003001, 0xffffffff));
            objectList.clear();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private List<Integer> parseObjects(byte[] rx_body)
    {
        List<Integer> directoryList = new ArrayList<>();
        try
        {
            int checkBytes = rx_body[0];
            int readPosition = checkBytes + 12;
            int nofSubDirectories =  ((int) rx_body[readPosition]) +
                    ((int) rx_body[readPosition + 1] << 8) +
                    ((int) rx_body[readPosition + 2] << 16) +
                    ((int) rx_body[readPosition + 3] << 24);
            for (int index = 0; index < nofSubDirectories; index++)
            {
                readPosition = readPosition + 4;
                int directoryId =  ((int) rx_body[readPosition]) +
                        ((int) rx_body[readPosition + 1] << 8) +
                        ((int) rx_body[readPosition + 2] << 16) +
                        ((int) rx_body[readPosition + 3] << 24);
                directoryList.add(directoryId);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.v(TAG, " Received Objects : " + directoryList.size() + " ");
        return (directoryList);
    }


    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            if (id == GET_STORAGE_HANDLE1)
            {
                // トップディレクトリを取得した
                List<Integer> directoriesList = parseObjects(rx_body);
                for (int subDirectory : directoriesList)
                {
                    Log.v(TAG, "  STORAGE ID : " + storageId + "  DIRECTORY ID : " + subDirectory);
                    publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_HANDLE2, isDumpLog, 0, 0x1007, 12, storageId, 0x00000000, subDirectory)); //
                }
                return;
            }
            if (id == GET_STORAGE_HANDLE2)
            {
                // サブディレクトリの一覧を受信した
                subDirectoryCount = 0;
                receivedDirectoryCount = 0;
                List<Integer> subDirectoriesList = parseObjects(rx_body);
                for (int subDirectory : subDirectoriesList)
                {
                    Log.v(TAG, "  STORAGE ID : " + storageId + "  DIRECTORY ID : " + subDirectory);
                    publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_HANDLE3, isDumpLog, 0, 0x1007, 12, storageId, 0x00000000, subDirectory)); //
                    subDirectoryCount++;
                }
                return;
            }
            if (id == GET_STORAGE_HANDLE3)
            {
                // OBJECT IDリストを受信した！
                List<Integer> objectsList = parseObjects(rx_body);
                objectList.addAll(objectsList);
                receivedDirectoryCount++;
                if (subDirectoryCount <= receivedDirectoryCount)
                {
                    // 送信要求したメッセージの応答をすべて受信した！
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

    List<Integer> getObjectIdList()
    {
        return (objectList);
    }

    boolean isObjectIdReceived()
    {
        return (isObjectReceived);
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
    {

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
