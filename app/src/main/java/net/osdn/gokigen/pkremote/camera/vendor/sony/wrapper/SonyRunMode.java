package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;

public class SonyRunMode implements ICameraRunMode
{
    @Override
    public void changeRunMode(boolean isRecording)
    {

    }

    @Override
    public boolean isRecordingMode()
    {
        return (false);
    }
}
