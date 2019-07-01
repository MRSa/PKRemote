package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.status.IFujiXRunModeHolder;

public class FujiXRunMode  implements ICameraRunMode, IFujiXRunModeHolder
{
    private final String TAG = toString();
    private boolean isChanging = false;
    private boolean isRecordingMode = false;

    FujiXRunMode()
    {
        //
    }

    @Override
    public void changeRunMode(boolean isRecording)
    {
        // 何もしない
        Log.v(TAG, "changeRunMode() : " + isRecording);
    }

    @Override
    public boolean isRecordingMode()
    {
        Log.v(TAG, "isRecordingMode() : " + isRecordingMode + " (" + isChanging + ")");

        if (isChanging)
        {
            // モード変更中の場合は、かならず false を応答する
            return (false);
        }
        return (isRecordingMode);
    }

    @Override
    public void transitToRecordingMode(boolean isFinished)
    {
        isChanging = !isFinished;
        isRecordingMode = true;
    }

    @Override
    public void transitToPlaybackMode(boolean isFinished)
    {
        isChanging = !isFinished;
        isRecordingMode = false;
    }
}
