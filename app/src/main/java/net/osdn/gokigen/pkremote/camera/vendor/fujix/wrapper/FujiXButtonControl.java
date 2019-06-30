package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl;

public class FujiXButtonControl implements ICameraButtonControl
{

    @Override
    public boolean pushedButton(String code, boolean isLongPress)
    {
        return (false);
    }
}
