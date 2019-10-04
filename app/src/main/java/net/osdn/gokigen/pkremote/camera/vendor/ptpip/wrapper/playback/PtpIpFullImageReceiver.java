package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IProgressEvent;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;

public class PtpIpFullImageReceiver implements IPtpIpCommandCallback
{
    private static final String TAG = PtpIpFullImageReceiver.class.getSimpleName();

    private final Activity activity;
    private final IPtpIpCommandPublisher publisher;
    private IDownloadContentCallback callback = null;
    private int objectId = 0;

    PtpIpFullImageReceiver(@NonNull Activity activity, @NonNull IPtpIpCommandPublisher publisher)
    {
        this.activity = activity;

        this.publisher = publisher;
    }

    void issueCommand(int objectId, int imageSize, IDownloadContentCallback callback)
    {
        if (this.objectId != 0)
        {
            // already issued
            Log.v(TAG, " COMMAND IS ALREADY ISSUED. : " + objectId);
            return;
        }
        this.callback = callback;
        Log.v(TAG, " GetPartialObject : " + objectId + " (size:" + imageSize + ")");
        publisher.enqueueCommand(new PtpIpCommandGeneric(this, (objectId + 1), true, objectId, 0x9107, 12, 0x01, 0x00, imageSize)); // 0x9107 : GetPartialObject
        this.objectId = objectId;
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            if (id == objectId + 1)
            {
                getPartialObjectEnd();
            }
            else if (id == objectId + 2)
            {
                Log.v(TAG, " RECEIVED  : " + id);

                // end of receive sequence.
                callback.onCompleted();
                objectId = 0;
                callback = null;
                System.gc();
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
    public void onReceiveProgress(final int currentBytes, final int totalBytes, byte[] rx_body)
    {
        int length = (rx_body == null) ? 0 : rx_body.length;
        Log.v(TAG, " onReceiveProgress() " + currentBytes + "/" + totalBytes + " (" + length + " bytes.)");
        callback.onProgress(rx_body, length, new IProgressEvent() {
            @Override
            public float getProgress() {
                return ((float) currentBytes / (float) totalBytes);
            }

            @Override
            public boolean isCancellable() {
                return (false);
            }

            @Override
            public void requestCancellation() {

            }
        });
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (true);
    }

    private void getPartialObjectEnd()
    {
        try
        {
            Log.v(TAG, " getPartialObjectEnd(), id : " + objectId);
            publisher.enqueueCommand(new PtpIpCommandGeneric(this,  (objectId + 2), true, objectId, 0x9117, 4,0x01));  // 0x9117 : TransferComplete
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.gc();
        }
    }
}
