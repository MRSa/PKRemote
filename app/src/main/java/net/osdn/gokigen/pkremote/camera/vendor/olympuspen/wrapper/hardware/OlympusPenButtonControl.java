package net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper.hardware;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl;

public class OlympusPenButtonControl implements ICameraButtonControl
{

    @Override
    public boolean pushedButton(String code, boolean isLongPress)
    {
        return (false);
    }
}
