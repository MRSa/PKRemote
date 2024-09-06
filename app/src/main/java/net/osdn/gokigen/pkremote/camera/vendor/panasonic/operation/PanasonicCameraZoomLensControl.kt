package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation

import android.util.Log
import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera

class PanasonicCameraZoomLensControl : IZoomLensControl
{
    private lateinit var camera: IPanasonicCamera
    private var isZooming = false

    fun setCamera(panasonicCamera: IPanasonicCamera)
    {
        camera = panasonicCamera
    }

    override fun canZoom(): Boolean
    {
        Log.v(TAG, "canZoom()")
        return (true)
    }

    override fun updateStatus()
    {
        Log.v(TAG, "updateStatus()")
    }

    override fun getMaximumFocalLength(): Float
    {
        Log.v(TAG, "getMaximumFocalLength()")
        return (0.0f)
    }

    override fun getMinimumFocalLength(): Float
    {
        Log.v(TAG, "getMinimumFocalLength()")
        return (0.0f)
    }

    override fun getCurrentFocalLength(): Float
    {
        Log.v(TAG, "getCurrentFocalLength()")
        return (0.0f)
    }

    override fun driveZoomLens(targetLength: Float)
    {
        Log.v(TAG, "driveZoomLens() : $targetLength")
    }

    override fun moveInitialZoomPosition()
    {
        Log.v(TAG, "moveInitialZoomPosition()")
    }

    override fun isDrivingZoomLens(): Boolean
    {
        Log.v(TAG, "isDrivingZoomLens()")
        return (isZooming)
    }

    override fun driveZoomLens(isZoomIn: Boolean)
    {
        Log.v(TAG, "driveZoomLens() : $isZoomIn")
        if (!::camera.isInitialized)
        {
            Log.v(TAG, "IPanasonicCameraApi is not initialized...")
            return
        }
        try
        {
            val command = if (isZooming) {
                "cam.cgi?mode=camcmd&value=zoomstop"
            }
            else
            {
                if ((isZoomIn)) "cam.cgi?mode=camcmd&value=tele-normal" else "cam.cgi?mode=camcmd&value=wide-normal"
            }
            val thread = Thread {
                try
                {
                    val sessionId = camera.getCommunicationSessionId()
                    val urlToSend = "${camera.getCmdUrl()}$command"
                    val reply = if (!sessionId.isNullOrEmpty())
                    {
                        val headerMap: MutableMap<String, String> = HashMap()
                        headerMap["X-SESSION_ID"] = sessionId
                        SimpleHttpClient.httpGetWithHeader(urlToSend, headerMap, null, TIMEOUT_MS)
                    }
                    else
                    {
                        SimpleHttpClient.httpGet(urlToSend, TIMEOUT_MS)
                    }
                    if (reply.contains("ok"))
                    {
                        isZooming = !isZooming
                    }
                    else
                    {
                        Log.v(TAG, "driveZoomLens() reply is failure.")
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private const val TIMEOUT_MS = 3000
        private val TAG: String = PanasonicCameraZoomLensControl::class.java.simpleName
    }
}
