package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera;

public class PanasonicCameraZoomLensControl implements IZoomLensControl
{
    private final String TAG = toString();
    private IPanasonicCamera camera = null;
    private boolean isZooming = false;
    private static final int TIMEOUT_MS = 3000;

    public PanasonicCameraZoomLensControl()
    {
        Log.v(TAG, "PanasonicCameraZoomLensControl()");
    }

    public void setCamera(@NonNull IPanasonicCamera panasonicCamera)
    {
        camera = panasonicCamera;
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
        return (0);
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
        return (isZooming);
    }

    /**
     *
     *
     */
    @Override
    public void driveZoomLens(boolean isZoomIn)
    {
        Log.v(TAG, "driveZoomLens() : " + isZoomIn);
        if (camera == null)
        {
            Log.v(TAG, "IPanasonicCameraApi is null...");
            return;
        }
        try
        {
            String command;
            if (isZooming)
            {
                command = "cam.cgi?mode=camcmd&value=zoomstop";
            }
            else
            {
                command = (isZoomIn) ? "cam.cgi?mode=camcmd&value=tele-normal" : "cam.cgi?mode=camcmd&value=wide-normal";
            }
            final String direction = command;
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String reply = SimpleHttpClient.httpGet(camera.getCmdUrl() + direction, TIMEOUT_MS);
                        if (reply.contains("ok"))
                        {
                            isZooming = !isZooming;
                        }
                        else
                        {
                            Log.v(TAG, "driveZoomLens() reply is failure.");
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
