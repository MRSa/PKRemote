package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;

import java.util.List;

public class CameraStatusHolder implements ICameraStatusHolder
{
    private static final String TAG = CameraStatusHolder.class.getSimpleName();
    private final Context context;
    private ICameraChangeListener listener = null;

    CameraStatusHolder(@NonNull Context context)
    {
        this.context = context;

    }

    void parse(String reply)
    {
        try
        {
            Log.v(TAG, " getState : " + reply);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void setEventChangeListener(@NonNull ICameraChangeListener listener)
    {
        this.listener = listener;
    }

    void clearEventChangeListener()
    {
        this.listener = null;
    }

    @Override
    public String getCameraStatus()
    {
        return (null);
    }

    @Override
    public boolean getLiveviewStatus()
    {
        return (false);
    }

    @Override
    public String getShootMode()
    {
        return (null);
    }

    @Override
    public List<String> getAvailableShootModes()
    {
        return (null);
    }

    @Override
    public int getZoomPosition()
    {
        return (0);
    }

    @Override
    public String getStorageId()
    {
        return (null);
    }
}
