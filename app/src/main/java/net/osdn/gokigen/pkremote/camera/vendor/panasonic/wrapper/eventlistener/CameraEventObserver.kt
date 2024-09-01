package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener

import android.content.Context
import android.util.Log
import net.osdn.gokigen.pkremote.ICardSlotSelector
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera

/**
 *
 *
 */
class CameraEventObserver private constructor(
    context: Context,
    private val remote: IPanasonicCamera,
    cardSlotSelector: ICardSlotSelector
) :
    ICameraEventObserver {
    private val statusHolder =
        CameraStatusHolder(context, remote, cardSlotSelector)
    private var isEventMonitoring: Boolean
    private var isActive: Boolean

    init {
        isEventMonitoring = false
        isActive = false
    }

    override fun start(): Boolean {
        if (!isActive) {
            Log.w(TAG, "start() observer is not active.")
            return (false)
        }
        if (isEventMonitoring) {
            Log.w(TAG, "start() already starting.")
            return (false)
        }
        isEventMonitoring = true

        try {
            val thread: Thread = object : Thread(
            ) {
                override fun run() {
                    Log.d(TAG, "start() exec.")
                    while (isEventMonitoring) {
                        try {
                            // parse reply message
                            statusHolder.parse(
                                SimpleHttpClient.httpGet(
                                    remote.getCmdUrl() + "cam.cgi?mode=getstate",
                                    TIMEOUT_MS
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            sleep(1000)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    isEventMonitoring = false
                }
            }
            thread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (true)
    }

    override fun stop() {
        isEventMonitoring = false
    }

    override fun release() {
        isEventMonitoring = false
        isActive = false
    }

    override fun setEventListener(listener: ICameraChangeListener) {
        try {
            statusHolder.setEventChangeListener(listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun clearEventListener() {
        try {
            statusHolder.clearEventChangeListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getCameraStatusHolder(): ICameraStatusHolder {
        return (statusHolder)
    }

    override fun activate() {
        isActive = true
    }

    companion object {
        private val TAG: String = CameraEventObserver::class.java.simpleName
        private const val TIMEOUT_MS = 3000
        fun newInstance(
            context: Context,
            apiClient: IPanasonicCamera,
            cardSlotSelector: ICardSlotSelector
        ): ICameraEventObserver {
            return (CameraEventObserver(context, apiClient, cardSlotSelector))
        }
    }
}