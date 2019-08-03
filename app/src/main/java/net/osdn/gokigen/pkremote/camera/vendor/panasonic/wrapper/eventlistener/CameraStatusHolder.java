package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener;

import java.util.List;

public class CameraStatusHolder implements ICameraStatusHolder
{
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
