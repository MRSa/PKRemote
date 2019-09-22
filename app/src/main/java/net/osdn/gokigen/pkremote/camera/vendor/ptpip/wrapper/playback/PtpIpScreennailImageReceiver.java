package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonRequestInnerDevelopEnd;


public class PtpIpScreennailImageReceiver  implements IPtpIpCommandCallback
{
    private static final String TAG = PtpIpScreennailImageReceiver.class.getSimpleName();

    private final IDownloadThumbnailImageCallback callback;
    private final IPtpIpCommandPublisher publisher;
    private final int objectId;

    PtpIpScreennailImageReceiver(int objectId, IPtpIpCommandPublisher publisher, IDownloadThumbnailImageCallback callback)
    {
        this.callback = callback;
        this.publisher = publisher;
        this.objectId = objectId;
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
                getPartialObject();
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
        if (rx_body == null)
        {
            Log.v(TAG, " " + currentBytes + "/" + totalBytes + " " + " NULL");
            return;
        }
        Log.v(TAG, " " + currentBytes + "/" + totalBytes + " " + rx_body.length + " bytes.");
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    private void requestGetPartialObject()
    {
        publisher.enqueueCommand(new PtpIpCommandGeneric(this, false, (objectId + 1), 0x9107, 12, 0x01, 0x00, 0x00200000));
    }

    private void getPartialObject()
    {
        publisher.enqueueCommand(new PtpIpCommandGeneric(this, false, (objectId + 2), 0x9117, 4,0x01));
    }

    private void requestInnerDevelopEnd()
    {
        publisher.enqueueCommand(new CanonRequestInnerDevelopEnd(this, true, (objectId + 3)));
    }

    private void finishedGetScreeennail()
    {
        Log.v(TAG, "  SCREENNAIL RECV FINISHED.");

        // リセットコマンドを送ってみる
        publisher.enqueueCommand(new PtpIpCommandGeneric(this, false, (objectId + 4), 0x902f));
    }

}
