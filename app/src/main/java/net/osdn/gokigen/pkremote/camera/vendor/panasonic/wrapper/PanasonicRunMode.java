package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.playback.PanasonicPlaybackControl;

public class PanasonicRunMode implements ICameraRunMode
{
    private final String TAG = toString();
    private boolean isRecordingMode = false;
    private IPanasonicCamera panasonicCamera = null;
    private PanasonicPlaybackControl playbackControl = null;
    private int timeoutMs = 50000;

    PanasonicRunMode()
    {
        //
    }

    void setCamera(IPanasonicCamera panasonicCamera, PanasonicPlaybackControl playbackControl, int timeoutMs)
    {
        this.panasonicCamera = panasonicCamera;
        this.playbackControl = playbackControl;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void changeRunMode(boolean isRecording)
    {
        try
        {
            String request = (isRecording) ? "recmode" : "playmode";
            String requestUrl = this.panasonicCamera.getCmdUrl() + "cam.cgi?mode=camcmd&value=" + request;

            // 撮影モード(RecMode)に切り替え
            String reply = SimpleHttpClient.httpGet(requestUrl, this.timeoutMs);
            if (!reply.contains("ok"))
            {
                Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE RECMODE.");
            }
            else
            {
                isRecordingMode = isRecording;
                if ((!isRecordingMode)&&(playbackControl != null))
                {
                    // 画像一覧の取得準備をする。。。
                    playbackControl.preprocessPlaymode();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRecordingMode()
    {
        return (isRecordingMode);
    }
}
