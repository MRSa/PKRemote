package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Nullable;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonRequestInnerDevelopEnd;

public class PtpIpScreennailImageReceiver  implements IPtpIpCommandCallback
{
    private static final String TAG = PtpIpScreennailImageReceiver.class.getSimpleName();
    private final Activity activity;
    private final IDownloadThumbnailImageCallback callback;
    private final IPtpIpCommandPublisher publisher;
    private final int objectId;

    PtpIpScreennailImageReceiver(Activity activity, int objectId, IPtpIpCommandPublisher publisher, IDownloadThumbnailImageCallback callback)
    {
        this.activity = activity;
        this.callback = callback;
        this.publisher = publisher;
        this.objectId = objectId;
        Log.v(TAG, "PtpIpScreennailImageReceiver CREATED : " + objectId);

    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            if (rx_body != null)
            {
                Log.v(TAG, "  receivedMessage() : " + id +  " " + rx_body.length + " bytes.");
            }
            else
            {
                Log.v(TAG, "  receivedMessage() : " + id + " NULL.");
            }
            if (id == objectId)
            {
                getRequestStatusEvent(rx_body);
            }
            else if (id == objectId + 1)
            {
                getPartialObject(rx_body);
            }
            else if (id == objectId + 2)
            {
                requestInnerDevelopEnd();
            }
            else if (id == objectId + 3)
            {
                finishedGetScreeennail();
            }
            else if (id == objectId + 4)
            {
                Log.v(TAG, " RECEIVED  : " + id);
            }
            else if (id == objectId + 5)
            {
                Log.v(TAG, " RECEIVED STATUS EVENT : " + id);
                requestGetPartialObject(rx_body);
            }
            else
            {
                Log.v(TAG, " RECEIVED UNKNOWN ID : " + id);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            {
                callback.onErrorOccurred(e);
            }
        }
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
    {
        int length = (rx_body == null) ? 0 : rx_body.length;
        Log.v(TAG, " onReceiveProgress() " + currentBytes + "/" + totalBytes + " (" + length + " bytes.)");
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    private void requestGetPartialObject(@Nullable byte[] rx_body)
    {
        Log.v(TAG, " requestGetPartialObject() : " + objectId);

        // 0x9107 : GetPartialObject  (元は 0x00020000)
        int pictureLength;
        if ((rx_body != null)&&(rx_body.length > 52))
        {
            int dataIndex = 48;
            pictureLength = (rx_body[dataIndex] & 0xff);
            pictureLength = pictureLength + ((rx_body[dataIndex + 1]  & 0xff) << 8);
            pictureLength = pictureLength + ((rx_body[dataIndex + 2] & 0xff) << 16);
            pictureLength = pictureLength + ((rx_body[dataIndex + 3] & 0xff) << 24);
        }
        else
        {
            pictureLength = 0x02000;
        }
        publisher.enqueueCommand(new PtpIpCommandGeneric(this, (objectId + 1), true, objectId, 0x9107, 12, 0x01, 0x00, pictureLength));
    }

    private void getRequestStatusEvent(byte[] rx_body)
    {
        Log.v(TAG, " getRequestStatusEvent  : " + objectId);
        publisher.enqueueCommand(new PtpIpCommandGeneric(this,  (objectId + 5), true, objectId, 0x9116));
    }

    private void getPartialObject(byte[] rx_body)
    {
        try
        {
            Log.v(TAG, " getPartialObject(), id : " + objectId + " size: " + rx_body.length);
            callback.onCompleted(BitmapFactory.decodeByteArray(rx_body, 0, rx_body.length), null);
            //callback.onCompleted(BitmapFactory.decodeStream(new ByteArrayInputStream(rx_body)), null);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.gc();
        }
        publisher.enqueueCommand(new PtpIpCommandGeneric(this,  (objectId + 2), true, objectId, 0x9117, 4,0x01));  // 0x9117 : TransferComplete

        // ファイルにバイナリデータをダンプする
        // binaryOutputToFile(activity, objectId + "_", rx_body);
    }

    private void requestInnerDevelopEnd()
    {
        Log.v(TAG, " requestInnerDevelopEnd() : " + objectId);
        publisher.enqueueCommand(new CanonRequestInnerDevelopEnd(this, (objectId + 3), true, objectId));  // 0x9143 : RequestInnerDevelopEnd
    }

    private void finishedGetScreeennail()
    {
        Log.v(TAG, "  --- SCREENNAIL RECV FINISHED. : " + objectId + " --- ");

        // リセットコマンドを送ってみる
        publisher.enqueueCommand(new PtpIpCommandGeneric(this, (objectId + 4), false, objectId, 0x902f));
    }

}
