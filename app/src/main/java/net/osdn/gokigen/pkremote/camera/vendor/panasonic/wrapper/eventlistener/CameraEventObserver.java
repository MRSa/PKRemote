package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera;

/**
 *
 *
 */
public class CameraEventObserver implements ICameraEventObserver
{
    private static final String TAG = CameraEventObserver.class.getSimpleName();
    private static final int TIMEOUT_MS = 3000;
    private final CameraStatusHolder statusHolder;
    private boolean isEventMonitoring;
    private boolean isActive;

    private final IPanasonicCamera remote;

    public static ICameraEventObserver newInstance(@NonNull Context context, @NonNull IPanasonicCamera apiClient)
    {
        return (new CameraEventObserver(context, apiClient));
    }

    private CameraEventObserver(@NonNull Context context, @NonNull IPanasonicCamera apiClient)
    {
        super();
        remote = apiClient;
        statusHolder = new CameraStatusHolder(context);
        isEventMonitoring = false;
        isActive = false;
    }

    @Override
    public boolean start()
    {
        if (!isActive)
        {
            Log.w(TAG, "start() observer is not active.");
            return (false);
        }
        if (isEventMonitoring)
        {
            Log.w(TAG, "start() already starting.");
            return (false);
        }
        isEventMonitoring = true;

        try
        {
            Thread thread = new Thread()
            {
                @Override
                public void run()
                {
                    Log.d(TAG, "start() exec.");
                    while (isEventMonitoring)
                    {
                        try
                        {
                            // parse reply message
                            statusHolder.parse(SimpleHttpClient.httpGet(remote.getCmdUrl() + "cam.cgi?mode=getstate", TIMEOUT_MS));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    isEventMonitoring = false;
                }
            };
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (true);
    }

    @Override
    public void stop()
    {
        isEventMonitoring = false;
    }

    @Override
    public void release()
    {
        isEventMonitoring = false;
        isActive = false;
    }

    @Override
    public void setEventListener(@NonNull ICameraChangeListener listener)
    {
        try
        {
            statusHolder.setEventChangeListener(listener);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void clearEventListener()
    {
        try
        {
            statusHolder.clearEventChangeListener();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public ICameraStatusHolder getCameraStatusHolder()
    {
        return (statusHolder);
    }

    @Override
    public void activate()
    {
        isActive = true;
    }
}
