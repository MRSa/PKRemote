package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener

import android.content.Context
import android.util.Log
import net.osdn.gokigen.pkremote.ICardSlotSelectionReceiver
import net.osdn.gokigen.pkremote.ICardSlotSelector
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera

class CameraStatusHolder internal constructor(
    private val context: Context,
    private val remote: IPanasonicCamera,
    private val cardSlotSelector: ICardSlotSelector
) :
    ICameraStatusHolder,
    ICardSlotSelectionReceiver {
    private var listener: ICameraChangeListener? = null
    private var currentSd = "sd1"
    private var isInitialized = false
    private var isDualSlot = false

    fun parse(reply: String) {
        try {
            var isEnableDualSlot = false
            if (reply.contains("<sd_memory>set</sd_memory>") && (reply.contains("<sd2_memory>set</sd2_memory>"))) {
                // カードが2枚刺さっている場合...
                isEnableDualSlot = true
            }
            if ((!isInitialized) || (isDualSlot != isEnableDualSlot)) {
                // 初回だけの実行...
                if (isEnableDualSlot) {
                    // カードが2枚刺さっている場合...
                    cardSlotSelector.setupSlotSelector(true, this)
                } else {
                    // カードが１つしか刺さっていない場合...
                    cardSlotSelector.setupSlotSelector(false, null)
                }
                isInitialized = true
                isDualSlot = isEnableDualSlot
            }
            checkCurrentSlot(reply)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkCurrentSlot(reply: String) {
        try {
            val header = "<current_sd>"
            val indexStart = reply.indexOf(header)
            val indexEnd = reply.indexOf("</current_sd>")
            if ((indexStart > 0) && (indexEnd > 0) && (indexStart < indexEnd)) {
                val currentSlot = reply.substring(indexStart + header.length, indexEnd)
                if (currentSd != currentSlot) {
                    currentSd = currentSlot
                    cardSlotSelector.changedCardSlot(currentSd)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEventChangeListener(listener: ICameraChangeListener) {
        this.listener = listener
    }

    fun clearEventChangeListener() {
        this.listener = null
    }

    override fun getCameraStatus(): String? {
        return (null)
    }

    override fun getLiveviewStatus(): Boolean {
        return (false)
    }

    override fun getShootMode(): String? {
        return (null)
    }

    override fun getAvailableShootModes(): List<String>? {
        return (null)
    }

    override fun getZoomPosition(): Int {
        return (0)
    }

    override fun getStorageId(): String {
        return (currentSd)
    }

    override fun slotSelected(slotId: String)
    {
        Log.v(TAG, " slotSelected : $slotId")
        if (currentSd != slotId)
        {
            // スロットを変更したい！
            requestToChangeSlot(slotId)
        }
    }

    private fun requestToChangeSlot(slotId: String)
    {
        try
        {
            val thread = Thread {
                try
                {
                    var loop = true
                    while (loop)
                    {
                        val urlToSend = remote.getCmdUrl() + "cam.cgi?mode=setsetting&type=current_sd&value=" + slotId
                        val sessionId = remote.getCommunicationSessionId()
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
                        if (reply.indexOf("<result>ok</result>") > 0)
                        {
                            loop = false
                            cardSlotSelector.selectSlot(slotId)
                        }
                        else
                        {
                            Thread.sleep(1000) // 1秒待つ
                        }
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
        private val TAG: String = CameraStatusHolder::class.java.simpleName
        private const val TIMEOUT_MS = 3000
    }
}