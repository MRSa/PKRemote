package net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.playback;

import android.util.Log;
import android.util.SparseArray;

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
    private int subDirectoryCount = 0;
    private int receivedDirectoryCount = 0;
    private boolean isObjectReceived = false;
    private SparseArray<NikonImageContentInfo> imageObjectList;

    NikonStorageContentHolder(@NonNull NikonInterfaceProvider provider, int storageId, @NonNull ImageObjectReceivedCallback callback)
    {
        this.provider = provider;
        this.storageId = storageId;
        this.callback = callback;
        this.imageObjectList = new SparseArray<>();
    }

    void getContents()
    {
        try
        {
            isObjectReceived = false;
            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_HANDLE1, isDumpLog, 0, 0x1007, 12, storageId, 0x00003001, 0xffffffff));
            imageObjectList.clear();
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
            int nofSubDirectories =  ((int) rx_body[readPosition] & 0x000000ff) +
                    (((int) rx_body[readPosition + 1] & 0x000000ff) << 8) +
                    (((int) rx_body[readPosition + 2] & 0x000000ff) << 16) +
                    (((int) rx_body[readPosition + 3] & 0x000000ff) << 24);
            for (int index = 0; index < nofSubDirectories; index++)
            {
                readPosition = readPosition + 4;
                byte data0 = rx_body[readPosition];
                byte data1 = rx_body[readPosition + 1];
                byte data2 = rx_body[readPosition + 2];
                byte data3 = rx_body[readPosition + 3];
                int objectId = ((int) data0 & 0x000000ff) + (((int) data1 & 0x000000ff) << 8) + (((int) data2 & 0x000000ff) << 16)+ (((int) data3 & 0x000000ff) << 24);
                directoryList.add(objectId);
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
                final List<Integer> subDirectoriesList = parseObjects(rx_body);
                for (final int subDirectory : subDirectoriesList)
                {
                    Log.v(TAG, "  STORAGE ID : " + storageId + "  DIRECTORY ID : " + subDirectory);

                    publisher.enqueueCommand(new PtpIpCommandGeneric(new IPtpIpCommandCallback() {
                        @Override
                        public void receivedMessage(int id, byte[] rx_body)
                        {
                            // OBJECT IDリストを受信した！
                            List<Integer> objectsList = parseObjects(rx_body);
                            for (int objectId : objectsList)
                            {
                                imageObjectList.append(objectId, new NikonImageContentInfo(storageId, subDirectory, objectId));
                            }
                            receivedDirectoryCount++;
                            if (subDirectoryCount <= receivedDirectoryCount)
                            {
                                // 送信要求したメッセージの応答をすべて受信した！
                                isObjectReceived = true;
                                callback.onReceived(storageId);
                            }
                        }

                        @Override
                        public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
                        {
                            //
                        }

                        @Override
                        public boolean isReceiveMulti() {
                            return false;
                        }
                    }, GET_STORAGE_HANDLE3, isDumpLog, 0, 0x1007, 12, storageId, 0x00000000, subDirectory));

/*
                    //  ストレージ名称。。。ここでは利用できない
                    publisher.enqueueCommand(new PtpIpCommandGeneric(new IPtpIpCommandCallback() {
                        @Override
                        public void receivedMessage(int id, byte[] rx_body)
                        {
                            try
                            {
                                //  ストレージIDの情報を取得  (名称、作成日時、更新日時）
                                String directoryName = parse2ByteCharacters(rx_body, ((int) rx_body[0] + 12 + 16 * 3 + 4));
                                String createTime =parse2ByteCharacters(rx_body, ((int) rx_body[0] + 12 + 16 * 4 + 7));
                                String modifyTime = parse2ByteCharacters(rx_body, ((int) rx_body[0] + 12 + 16 * 6 + 8));
                                Log.v(TAG, " ST: " + storageId + " SD : " + subDirectory + " " + directoryName + " " + createTime + " " + modifyTime);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
                        {
                            // なにもしない
                        }

                        @Override
                        public boolean isReceiveMulti()
                        {
                            return (false);
                        }
                    }, GET_FOLDER_INFO, isDumpLog, 0, 0x1008, 4, subDirectory));
*/
                    subDirectoryCount++;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callback.onError(e);
        }
    }

/*
    private String parse2ByteCharacters(byte[] rx_body, int startPosition)
    {
        int length = (int) rx_body[startPosition] - 1;
        startPosition++;

        String value = "";
        try
        {
            byte[] data = new byte[length];
            for (int index = 0; index < length; index++)
            {
                data[index] = rx_body[startPosition + index * 2];
            }
            value = new String(data);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (value);
    }
*/

    SparseArray<NikonImageContentInfo> getObjectIdList()
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
