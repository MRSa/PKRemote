package net.osdn.gokigen.pkremote.camera.vendor.ricoh.wrapper;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;

public class RicohGr2RunMode implements ICameraRunMode
{
    @Override
    public void changeRunMode(boolean isRecording)
    {
        // 何もしない...
    }

    @Override
    public boolean isRecordingMode()
    {
        return (true);
    }
}
