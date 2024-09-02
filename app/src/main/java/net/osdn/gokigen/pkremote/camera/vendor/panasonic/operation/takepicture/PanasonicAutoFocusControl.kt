package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.takepicture

import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay.FocusFrameStatus
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera
import kotlin.math.floor

class PanasonicAutoFocusControl(private val frameDisplayer: IAutoFocusFrameDisplay, private val indicator: IIndicatorControl)
{
    private lateinit var camera: IPanasonicCamera

    fun setCamera(panasonicCamera: IPanasonicCamera)
    {
        this.camera = panasonicCamera
    }

    fun lockAutoFocus(point: PointF)
    {
        Log.v(TAG, "lockAutoFocus() : [" + point.x + ", " + point.y + "]")
        if (!::camera.isInitialized)
        {
            Log.v(TAG, "IPanasonicCamera is not initialized...")
            return
        }
        try
        {
            val thread = Thread {
                val preFocusFrameRect = getPreFocusFrameRect(point)
                try
                {
                    showFocusFrame(preFocusFrameRect, FocusFrameStatus.Running, 0.0)
                    val posX = floor(point.x * 1000.0).toInt()
                    val posY = floor(point.y * 1000.0).toInt()
                    Log.v(TAG, "AF ($posX, $posY)")

                    val sessionId = camera.getCommunicationSessionId()
                    val urlToSend = "${camera.getCmdUrl()}cam.cgi?mode=camctrl&type=touch&value=$posX/$posY&value2=on"
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
                    if (!reply.contains("ok"))
                    {
                        Log.v(TAG, "setTouchAFPosition() reply is null.")
                    }
                    showFocusFrame(preFocusFrameRect, FocusFrameStatus.Errored, 1.0)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                    try
                    {
                        showFocusFrame(preFocusFrameRect, FocusFrameStatus.Errored, 1.0)
                    }
                    catch (ee: Exception)
                    {
                        ee.printStackTrace()
                    }
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * シャッター半押し処理
     */
    fun halfPressShutter(isPressed: Boolean)
    {
        Log.v(TAG, "halfPressShutter() : $isPressed")
        if (!::camera.isInitialized)
        {
            Log.v(TAG, "IPanasonicCamera is not initialized...")
            return
        }
        try
        {
            val thread = Thread {
                try
                {
                    val status = if ((isPressed)) "on" else "off"
                    val sessionId = camera.getCommunicationSessionId()
                    val urlToSend = "${camera.getCmdUrl()}cam.cgi?mode=camctrl&type=touch&value=500/500&value2=$status"
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

                    if (!reply.contains("ok"))
                    {
                        Log.v(TAG, "CENTER FOCUS ($status) FAIL...")
                    }
                    else
                    {
                        indicator.onAfLockUpdate(isPressed)
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

    /**
     *
     *
     */
    fun unlockAutoFocus() {
        Log.v(TAG, "unlockAutoFocus()")
        if (!::camera.isInitialized)
        {
            Log.v(TAG, "IPanasonicCamera is not initialized...")
            return
        }
        try
        {
            val thread = Thread {
                try
                {
                    val sessionId = camera.getCommunicationSessionId()
                    val urlToSend = "${camera.getCmdUrl()}cam.cgi?mode=camctrl&type=touch&value=500/500&value2=off"
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
                    if (!reply.contains("ok"))
                    {
                        Log.v(TAG, "CENTER FOCUS (UNLOCK) FAIL...")
                    }
                    hideFocusFrame()
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

    private fun showFocusFrame(rect: RectF, status: FocusFrameStatus, duration: Double)
    {
        try
        {
            frameDisplayer.showFocusFrame(rect, status, duration)
            indicator.onAfLockUpdate(FocusFrameStatus.Focused == status)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun hideFocusFrame()
    {
        try
        {
            frameDisplayer.hideFocusFrame()
            indicator.onAfLockUpdate(false)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun getPreFocusFrameRect(point: PointF): RectF
    {
        val imageWidth = frameDisplayer.contentSizeWidth
        val imageHeight = frameDisplayer.contentSizeHeight

        // Display a provisional focus frame at the touched point.
        val focusWidth = 0.125f // 0.125 is rough estimate.
        var focusHeight = 0.125f
        focusHeight *= if (imageWidth > imageHeight) {
            imageWidth / imageHeight
        } else {
            imageHeight / imageWidth
        }
        return (RectF(
            point.x - focusWidth / 2.0f, point.y - focusHeight / 2.0f,
            point.x + focusWidth / 2.0f, point.y + focusHeight / 2.0f
        ))
    }

    companion object
    {
        private val TAG: String = PanasonicAutoFocusControl::class.java.simpleName
        private const val TIMEOUT_MS = 3000
/*
        private fun findTouchAFPositionResult(replyJson: JSONObject): Boolean
        {
            var afResult = false
            try
            {
                val indexOfTouchAFPositionResult = 1
                val resultsObj = replyJson.getJSONArray("result")
                if (!resultsObj.isNull(indexOfTouchAFPositionResult))
                {
                    val touchAFPositionResultObj = resultsObj.getJSONObject(indexOfTouchAFPositionResult)
                    afResult = touchAFPositionResultObj.getBoolean("AFResult")
                    Log.v(TAG, "AF Result : $afResult")
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            return (afResult)
        }
*/
    }
}