package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl;

public class SonyButtonControl implements ICameraButtonControl
{
    @Override
    public boolean pushedButton(String code, boolean isLongPress)
    {
        return (false);
    }
}
