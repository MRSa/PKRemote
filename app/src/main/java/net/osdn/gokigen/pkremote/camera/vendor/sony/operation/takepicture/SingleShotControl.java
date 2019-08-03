package net.osdn.gokigen.pkremote.camera.vendor.sony.operation.takepicture;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraApi;

import org.json.JSONObject;

public class SingleShotControl
{
    private static final String TAG = SingleShotControl.class.getSimpleName();
    private final IAutoFocusFrameDisplay frameDisplayer;
    private final IIndicatorControl indicator;
    private ISonyCameraApi cameraApi = null;

    /**
     *
     *
     */
    public SingleShotControl(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator)
    {
        this.frameDisplayer = frameDisplayer;
        this.indicator = indicator;
    }

    /**
     *
     *
     */
    public void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi)
    {
        this.cameraApi = sonyCameraApi;
    }

    /**
     *
     *
     */
    public void singleShot()
    {
        Log.v(TAG, "singleShot()");
        if (cameraApi == null)
        {
            Log.v(TAG, "ISonyCameraApi is null...");
            return;
        }
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        //JSONObject resultsObj = cameraApi.awaitTakePicture();
                        JSONObject resultsObj = cameraApi.actTakePicture();
                        if (resultsObj == null)
                        {
                            Log.v(TAG, "setTouchAFPosition() reply is null.");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    frameDisplayer.hideFocusFrame();
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
