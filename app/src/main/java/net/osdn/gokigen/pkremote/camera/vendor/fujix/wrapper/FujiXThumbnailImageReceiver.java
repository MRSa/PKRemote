package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;

public class FujiXThumbnailImageReceiver implements IFujiXCommandCallback
{
    private final String TAG = toString();
    private final Context context;
    private final IDownloadThumbnailImageCallback callback;

    FujiXThumbnailImageReceiver(Context context, @NonNull IDownloadThumbnailImageCallback callback)
    {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            int offset = 12;
            if (rx_body.length > offset)
            {
                callback.onCompleted(BitmapFactory.decodeByteArray(rx_body, offset, rx_body.length - offset), null);
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
}
