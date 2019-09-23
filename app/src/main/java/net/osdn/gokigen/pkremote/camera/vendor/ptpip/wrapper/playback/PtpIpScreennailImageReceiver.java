package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonRequestInnerDevelopEnd;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static net.osdn.gokigen.pkremote.camera.utils.SimpleLogDumper.binaryOutputToFile;


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
                requestGetPartialObject();
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

    private void requestGetPartialObject()
    {
        Log.v(TAG, " requestGetPartialObject() : " + objectId);
        //publisher.enqueueCommand(new PtpIpCommandGeneric(this, false, (objectId + 1), 0x9107, 12, 0x01, 0x00, 0x00200000));
    }

    private void getPartialObject(byte[] rx_body)
    {
        Log.v(TAG, " getPartialObject(), id : " + objectId + " size: " + rx_body.length);
        callback.onCompleted(BitmapFactory.decodeStream(new ByteArrayInputStream(rx_body)), null);
        //publisher.enqueueCommand(new PtpIpCommandGeneric(this, false, (objectId + 2), 0x9117, 4,0x01));

        // ファイルにバイナリデータをダンプする
        //binaryOutputToFile(activity, objectId + "_", rx_body);
    }

    private void requestInnerDevelopEnd()
    {
        Log.v(TAG, " requestInnerDevelopEnd() : " + objectId);
        //publisher.enqueueCommand(new CanonRequestInnerDevelopEnd(this, true, (objectId + 3)));
    }

    private void finishedGetScreeennail()
    {
        Log.v(TAG, "  --- SCREENNAIL RECV FINISHED. : " + objectId + " --- ");

        // リセットコマンドを送ってみる
        publisher.enqueueCommand(new PtpIpCommandGeneric(this, false, (objectId + 4), 0x902f));
    }

}
