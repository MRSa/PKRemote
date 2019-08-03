package net.osdn.gokigen.pkremote.camera.vendor.sony.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.vendor.sony.operation.takepicture.SingleShotControl;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraApi;

public class SonyCameraCaptureControl implements ICaptureControl
{
    private static final String TAG = SonyCameraCaptureControl.class.getSimpleName();
    private final SingleShotControl singleShotControl;

    public SonyCameraCaptureControl(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator)
    {
        singleShotControl = new SingleShotControl(frameDisplayer, indicator);
    }

    public void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi)
    {
        singleShotControl.setCameraApi(sonyCameraApi);
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
