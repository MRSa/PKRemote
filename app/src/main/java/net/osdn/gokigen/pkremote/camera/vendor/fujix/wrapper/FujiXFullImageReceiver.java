package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IProgressEvent;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;

import java.util.Arrays;

public class FujiXFullImageReceiver implements IFujiXCommandCallback
{
    private final String TAG = toString();
    private final Context context;
    private final IDownloadContentCallback callback;

    FujiXFullImageReceiver(Context context, @NonNull IDownloadContentCallback callback)
    {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            Log.v(TAG, " receivedMessage() " + id + " / " + rx_body.length + " bytes.");
            int offset = 12;
            if (rx_body.length > offset)
            {
                callback.onProgress(Arrays.copyOfRange(rx_body, offset, (rx_body.length - offset)), rx_body.length, new IProgressEvent() {
                    @Override
                    public float getProgress() {
                        return (1.0f);
                    }

                    @Override
                    public boolean isCancellable() {
                        return (false);
                    }

                    @Override
                    public void requestCancellation() { }
                });
                callback.onCompleted();
            }
            else
            {
                Log.v(TAG, "ERROR RESPONSE... : " + rx_body.length);
                callback.onErrorOccurred(new NullPointerException());
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
    public void onReceiveProgress(final int currentBytes, final int totalBytes)
    {
        try
        {
            Log.v(TAG, " onReceiveProgress() " + currentBytes + "/" + totalBytes);

            callback.onProgress(null, currentBytes, new IProgressEvent() {
                @Override
                public float getProgress() {
                    return( (float) currentBytes / (float) totalBytes);
                }

                @Override
                public boolean isCancellable() {
                    return (false);
                }

                @Override
                public void requestCancellation() { }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callback.onErrorOccurred(e);
        }
    }



}
