package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper

import android.util.Log
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.playback.PanasonicPlaybackControl

class PanasonicRunMode internal constructor() : ICameraRunMode
{
    private var isRecordingMode = false
    private var panasonicCamera: IPanasonicCamera? = null
    private var playbackControl: PanasonicPlaybackControl? = null
    private var timeoutMs = TIMEOUT_MS

    fun setCamera(panasonicCamera: IPanasonicCamera?, playbackControl: PanasonicPlaybackControl?, timeoutMs: Int = TIMEOUT_MS)
    {
        this.panasonicCamera = panasonicCamera
        this.playbackControl = playbackControl
        this.timeoutMs = timeoutMs
    }

    override fun changeRunMode(isRecording: Boolean)
    {
        try
        {
            val request = if ((isRecording)) "recmode" else "playmode"
            val requestUrl = panasonicCamera?.getCmdUrl() + "cam.cgi?mode=camcmd&value=" + request

            // 撮影モード(RecMode)に切り替え
            val reply = SimpleHttpClient.httpGet(requestUrl, this.timeoutMs)
            if (!reply.contains("ok"))
            {
                Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE RECMODE.")
            }
            else
            {
                isRecordingMode = isRecording
                if ((!isRecordingMode) && (playbackControl != null))
                {
                    // 画像一覧の取得準備をする。。。
                    playbackControl?.preprocessPlaymode()
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun isRecordingMode(): Boolean
    {
        return (isRecordingMode)
    }

    companion object
    {
        private const val TIMEOUT_MS = 50000
        private val TAG = PanasonicRunMode::class.java.simpleName
    }
}
