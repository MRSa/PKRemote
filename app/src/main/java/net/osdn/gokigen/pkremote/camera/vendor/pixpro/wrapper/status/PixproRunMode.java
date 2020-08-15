package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.status;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;

public class PixproRunMode implements ICameraRunMode
{
    private boolean runMode = false;

    public PixproRunMode()
    {
        //
    }

    @Override
    public void changeRunMode(boolean isRecording)
    {
        this.runMode = isRecording;
    }

    @Override
    public boolean isRecordingMode()
    {
        return (runMode);
    }
}
