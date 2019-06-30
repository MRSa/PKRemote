package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;

public class FujiXRunMode  implements ICameraRunMode
{
    FujiXRunMode()
    {
        //
    }

    @Override
    public void changeRunMode(boolean isRecording)
    {
        // 何もしない
    }

    @Override
    public boolean isRecordingMode()
    {
        // シーケンスを入れる
        return (true);
    }
}
