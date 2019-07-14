package net.osdn.gokigen.pkremote.camera.vendor.fujix.operation;


import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.CaptureCommand;

public class FujiXCaptureControl implements ICaptureControl, IFujiXCommandCallback
{
    private final String TAG = this.toString();
    private final IFujiXCommandPublisher issuer;
    private final IAutoFocusFrameDisplay frameDisplay;


    public FujiXCaptureControl(@NonNull IFujiXCommandPublisher issuer, IAutoFocusFrameDisplay frameDisplay)
    {
        this.issuer = issuer;
        this.frameDisplay = frameDisplay;

    }

    @Override
    public void doCapture(int kind)
    {
        try
        {
            boolean ret = issuer.enqueueCommand(new CaptureCommand(this));
            if (!ret)
            {
                Log.v(TAG, "enqueue ERROR");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        Log.v(TAG, "Response Received.");
        frameDisplay.hideFocusFrame();
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
