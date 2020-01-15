package net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;

public class ThetaRunMode implements ICameraRunMode
{
    private boolean runMode = false;

    ThetaRunMode()
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
