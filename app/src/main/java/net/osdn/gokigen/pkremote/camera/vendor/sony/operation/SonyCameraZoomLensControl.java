package net.osdn.gokigen.pkremote.camera.vendor.sony.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraApi;

import org.json.JSONObject;

public class SonyCameraZoomLensControl implements IZoomLensControl
{
    private final String TAG = toString();
    private ISonyCameraApi cameraApi = null;

    public SonyCameraZoomLensControl()
    {
        Log.v(TAG, "SonyCameraZoomLensControl()");
    }

    public void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi)
    {
        cameraApi = sonyCameraApi;
    }

    @Override
    public boolean canZoom() {
        Log.v(TAG, "canZoom()");
        return (true);
    }

    @Override
    public void updateStatus()
    {
        Log.v(TAG, "updateStatus()");
    }

    @Override
    public float getMaximumFocalLength()
    {
        Log.v(TAG, "getMaximumFocalLength()");
        return (0);
    }

    @Override
    public float getMinimumFocalLength()
    {
        Log.v(TAG, "getMinimumFocalLength()");
        return (0);
    }

    @Override
    public float getCurrentFocalLength()
    {
        Log.v(TAG, "getCurrentFocalLength()");
        return 0;
    }

    @Override
    public void driveZoomLens(float targetLength)
    {
        Log.v(TAG, "driveZoomLens() : " + targetLength);
    }

    @Override
    public void moveInitialZoomPosition()
    {
        Log.v(TAG, "moveInitialZoomPosition()");
    }

    @Override
    public boolean isDrivingZoomLens()
    {
        Log.v(TAG, "isDrivingZoomLens()");
        return (false);
    }

    /**
     *
     *
     */
    @Override
    public void driveZoomLens(boolean isZoomIn)
    {
        Log.v(TAG, "driveZoomLens() : " + isZoomIn);
        if (cameraApi == null)
        {
            Log.v(TAG, "ISonyCameraApi is null...");
            return;
        }
        try
        {
            final String direction = (isZoomIn) ? "in" : "out";
            final String movement = "1shot";
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        JSONObject resultsObj = cameraApi.actZoom(direction, movement);
                        if (resultsObj == null)
                        {
                            Log.v(TAG, "driveZoomLens() reply is null.");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
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
