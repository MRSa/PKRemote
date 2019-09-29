package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;

import java.util.Arrays;

/**
 *   Canonサムネイル画像の受信
 *
 *
 */
public class PtpIpThumbnailImageReceiver implements IPtpIpCommandCallback
{
    private final String TAG = toString();
    private final Activity context;
    private final IDownloadThumbnailImageCallback callback;

    PtpIpThumbnailImageReceiver(@NonNull Activity context, @NonNull IDownloadThumbnailImageCallback callback)
    {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            if (rx_body == null)
            {
                Log.v(TAG, " BITMAP IS NONE...");
                callback.onCompleted(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_broken_image_black_24dp), null);
                return;
            }

            /////// 受信データから、サムネイルの先頭(0xff 0xd8)を検索する  /////
            int offset = rx_body.length - 22;
            //byte[] thumbnail0 = Arrays.copyOfRange(rx_body, 0, rx_body.length);
            while (offset > 32)
            {
                if ((rx_body[offset] == (byte) 0xff)&&((rx_body[offset + 1] == (byte) 0xd8)))
                {
                    break;
                }
                offset--;
            }
            byte[] thumbnail = Arrays.copyOfRange(rx_body, offset, rx_body.length - 22);
            //binaryOutputToFile(context, id + "_", thumbnail);
            callback.onCompleted(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length), null);
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
