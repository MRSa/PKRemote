package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status.IPtpIpRunModeHolder;

public class PtpIpRunMode implements ICameraRunMode, IPtpIpRunModeHolder
{
    private final String TAG = toString();
    private boolean isChanging = false;
    private boolean isRecordingMode = false;

    public PtpIpRunMode()
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
