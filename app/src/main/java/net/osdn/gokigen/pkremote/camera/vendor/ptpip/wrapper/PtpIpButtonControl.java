package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl;

public class PtpIpButtonControl implements ICameraButtonControl
{

    @Override
    public boolean pushedButton(String code, boolean isLongPress)
    {
        return (false);
    }
}
