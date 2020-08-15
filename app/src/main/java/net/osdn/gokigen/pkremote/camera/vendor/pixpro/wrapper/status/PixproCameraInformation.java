package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.status;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraInformation;

public class PixproCameraInformation implements ICameraInformation
{

    @Override
    public boolean isManualFocus()
    {
        return false;
    }

    @Override
    public boolean isElectricZoomLens()
    {
        return false;
    }

    @Override
    public boolean isExposureLocked()
    {
        return false;
    }
}
