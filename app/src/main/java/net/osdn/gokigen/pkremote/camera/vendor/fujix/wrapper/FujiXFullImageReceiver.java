package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IProgressEvent;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;

public class FujiXFullImageReceiver implements IFujiXCommandCallback
{
    private final String TAG = toString();
    private final IDownloadContentCallback callback;
    private int receivedLength;

    FujiXFullImageReceiver( @NonNull IDownloadContentCallback callback)
    {
        this.callback = callback;
        this.receivedLength = 0;
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            Log.v(TAG, " receivedMessage() : onCompleted. " + id + " (" + receivedLength + " bytes.)");
            callback.onCompleted();
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
    public void onReceiveProgress(final int currentBytes, final int totalBytes, byte[] body)
    {
        try
        {
            receivedLength = receivedLength + currentBytes;
            //Log.v(TAG, " onReceiveProgress() " + receivedLength + "/" + totalBytes);

            callback.onProgress(body, currentBytes, new IProgressEvent() {
                @Override
                public float getProgress() {
                    return( (float) receivedLength / (float) totalBytes);
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

    @Override
    public boolean isReceiveMulti()
    {
        return (true);
    }

}
