package net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.hardware;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl;

public class ThetaButtonControl implements ICameraButtonControl
{
    @Override
    public boolean pushedButton(String code, boolean isLongPress)
    {
        return (false);
    }
}
