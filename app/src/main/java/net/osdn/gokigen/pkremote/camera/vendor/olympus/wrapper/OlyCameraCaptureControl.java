package net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.vendor.olympus.operation.takepicture.SingleShotControl;

public class OlyCameraCaptureControl implements ICaptureControl
{
    private final String TAG = toString();
    private final SingleShotControl singleShotControl;

    OlyCameraCaptureControl(OlyCameraWrapper wrapper, IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator)
    {
        singleShotControl = new SingleShotControl(wrapper.getOLYCamera(), frameDisplayer, indicator);
    }

    /**
     *   撮影する
     *
     */
    @Override
    public void doCapture(int kind)
    {
        Log.v(TAG, "doCapture() : " + kind);
        try
        {
            singleShotControl.singleShot();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
