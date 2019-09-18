package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.app.Activity;
import android.util.Log;
import android.util.SparseArray;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.utils.SimpleLogDumper;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.PtpIpInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;

import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_STORAGE_ID;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_STORAGE_INFO;
import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_OBJECT_INFO_EX;

public class CanonImageObjectReceiver implements IPtpIpCommandCallback
{
    private final String TAG = toString();
    private final Activity activity;
    private final PtpIpInterfaceProvider provider;

    CanonImageObjectReceiver(Activity activity, PtpIpInterfaceProvider provider)
    {
        this.activity = activity;
        this.provider = provider;

    }


    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            SimpleLogDumper.dump_bytes(" [RX] ", rx_body);
            switch (id)
            {
                case GET_STORAGE_ID:
                    publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_INFO, 0x9102, 4, 0x00010001));
                    break;

                case GET_STORAGE_INFO:
                    publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_OBJECT_INFO_EX, 0x9109, 12, 0x00010001, 0xffffffff, 0x00200000));
                    break;

                case GET_OBJECT_INFO_EX:
                default:
                    break;
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

    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    void getCameraContents(SparseArray<PtpIpImageContentInfo> imageContentInfo, ICameraContentListCallback callback)
    {
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
                publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_STORAGE_ID, 0x9101));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

/*
    void getCameraContents(SparseArray<PtpIpImageContentInfo> imageContentInfo, ICameraContentListCallback callback)
    {
        int nofFiles = -1;
        try
        {
            finishedCallback = callback;
            ICameraStatus statusListHolder = provider.getCameraStatusListHolder();
            if (statusListHolder != null) {
                String count = statusListHolder.getStatus(IMAGE_FILE_COUNT_STR_ID);
                nofFiles = Integer.parseInt(count);
                Log.v(TAG, "getCameraContents() : " + nofFiles + " (" + count + ")");
            }
            Log.v(TAG, "getCameraContents() : DONE.");
            if (nofFiles > 0)
            {
                // 件数ベースで取得する(情報は、後追いで反映させる...この方式だと、キューに積みまくってるが、、、)
                checkImageFiles(nofFiles);
            }
            else
            {
                // 件数が不明だったら、１件づつインデックスの情報を取得する
                checkImageFileAll();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            finishedCallback.onErrorOccurred(e);
            finishedCallback = null;
        }
    }
*/

    /**
     *   最初から取得可能なイメージ情報を(件数ベースで)取得する
     *
     */
/*
    private void checkImageFiles(int nofFiles)
    {
        try
        {
            imageContentInfo.clear();
            //IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            //for (int index = nofFiles; index > 0; index--)
            for (int index = 1; index <= nofFiles; index++)
            {
                // ファイル数分、仮のデータを生成する
                imageContentInfo.append(index, new PtpIpImageContentInfo(index, null));

                //ファイル名などを取得する (メッセージを積んでおく...でも遅くなるので、ここではやらない方がよいかな。）
                //publisher.enqueueCommand(new GetImageInfo(index, index, info));
            }

            // インデックスデータがなくなったことを検出...データがそろったとして応答する。
            Log.v(TAG, "IMAGE LIST : " + imageContentInfo.size() + " (" + nofFiles + ")");
            finishedCallback.onCompleted(getCameraContentList());
            finishedCallback = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
*/

    /**
     *   最初から取得可能なイメージ情報をすべて取得する
     *
     */
    private void checkImageFileAll()
    {
        try
        {
/*
            imageContentInfo.clear();
            indexNumber = 1;
            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            publisher.enqueueCommand(new GetImageInfo(indexNumber, indexNumber, this));
*/
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



}
