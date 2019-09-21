package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;

import java.io.ByteArrayInputStream;

public class PtpIpThumbnailImageReceiver implements IPtpIpCommandCallback
{
    private final String TAG = toString();
    private final Context context;
    private final IDownloadThumbnailImageCallback callback;

    PtpIpThumbnailImageReceiver(@NonNull Context context, @NonNull IDownloadThumbnailImageCallback callback)
    {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            int offset = 32;
            Log.v(TAG, "  RECV THUMBNAIL : " + id + " [" + rx_body.length + "] " + offset);
            if (rx_body.length > offset)
            {
                //callback.onCompleted(BitmapFactory.decodeByteArray(rx_body, offset, rx_body.length - offset - 22), null);
                callback.onCompleted(BitmapFactory.decodeStream(new ByteArrayInputStream(rx_body, offset, rx_body.length)), null);
                //callback.onCompleted(BitmapFactory.decodeStream(new ByteArrayInputStream(rx_body, 0, rx_body.length)), null);
            }
            else
            {
                Log.v(TAG, "BITMAP IS NONE... : " + rx_body.length);
                callback.onCompleted(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_broken_image_black_24dp), null);
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
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }
}
