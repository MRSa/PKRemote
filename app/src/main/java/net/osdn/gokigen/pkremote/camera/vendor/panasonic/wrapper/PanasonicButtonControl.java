package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl;

public class PanasonicButtonControl implements ICameraButtonControl
{

    @Override
    public boolean pushedButton(String code, boolean isLongPress)
    {
        return (false);
    }
}
