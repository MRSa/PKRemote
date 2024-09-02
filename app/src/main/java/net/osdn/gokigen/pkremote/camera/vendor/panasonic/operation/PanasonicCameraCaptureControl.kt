package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.takepicture.SingleShotControl;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera;

public class PanasonicCameraCaptureControl implements ICaptureControl
{
    private static final String TAG = PanasonicCameraCaptureControl.class.getSimpleName();
    private final SingleShotControl singleShotControl;

    public PanasonicCameraCaptureControl(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator)
    {
        singleShotControl = new SingleShotControl(frameDisplayer, indicator);
    }

    public void setCamera(@NonNull IPanasonicCamera panasonicCamera)
    {
        singleShotControl.setCamera(panasonicCamera);
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
