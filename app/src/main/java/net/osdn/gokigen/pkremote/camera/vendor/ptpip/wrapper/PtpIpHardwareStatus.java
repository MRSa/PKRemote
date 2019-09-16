package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraHardwareStatus;

import java.util.Map;

public class PtpIpHardwareStatus implements ICameraHardwareStatus
{
    @Override
    public boolean isAvailableHardwareStatus()
    {
        return (false);
    }

    @Override
    public String getLensMountStatus()
    {
        return (null);
    }

    @Override
    public String getMediaMountStatus()
    {
        return (null);
    }

    @Override
    public float getMinimumFocalLength()
    {
        return (0);
    }

    @Override
    public float getMaximumFocalLength()
    {
        return (0);
    }

    @Override
    public float getActualFocalLength()
    {
        return (0);
    }

    @Override
    public Map<String, Object> inquireHardwareInformation()
    {
        return (null);
    }

}
