package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.takepicture;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera;

public class SingleShotControl
{
    private static final String TAG = SingleShotControl.class.getSimpleName();
    private static final int TIMEOUT_MS = 3000;
    private final IAutoFocusFrameDisplay frameDisplayer;
    private final IIndicatorControl indicator;
    private IPanasonicCamera camera = null;

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
    public void setCamera(@NonNull IPanasonicCamera panasonicCamera)
    {
        this.camera = panasonicCamera;
    }

    /**
     *
     *
     */
    public void singleShot()
    {
        Log.v(TAG, "singleShot()");
        if (camera == null)
        {
            Log.v(TAG, "IPanasonicCamera is null...");
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
                        String reply = SimpleHttpClient.httpGet(camera.getCmdUrl() + "cam.cgi?mode=camcmd&value=capture", TIMEOUT_MS);
                        if (!reply.contains("ok"))
                        {
                            Log.v(TAG, "Capture Failure... : " + reply);
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
