package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper

import android.app.Activity
import android.util.Log
import net.osdn.gokigen.pkremote.ICardSlotSelector
import net.osdn.gokigen.pkremote.IInformationReceiver
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingModeNotify
import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraStatusUpdateNotify
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IDisplayInjector
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraHardwareStatus
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraInformation
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.IPanasonicInterfaceProvider
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.PanasonicCameraCaptureControl
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.PanasonicCameraFocusControl
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.PanasonicCameraZoomLensControl
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.connection.PanasonicCameraConnection
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener.CameraEventObserver
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener.ICameraEventObserver
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener.PanasonicStatus
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.playback.PanasonicPlaybackControl

class PanasonicCameraWrapper(
    private val context: Activity,
    private val provider: ICameraStatusReceiver,
    private val listener: ICameraChangeListener,
    informationReceiver: IInformationReceiver,
    private val cardSlotSelector: ICardSlotSelector
) : IPanasonicCameraHolder, IPanasonicInterfaceProvider, IDisplayInjector
{
    private var panasonicCamera: IPanasonicCamera? = null
    private var eventObserver: ICameraEventObserver? = null
    private var liveViewControl: PanasonicLiveViewControl? = null
    private var focusControl: PanasonicCameraFocusControl? = null
    private var captureControl: PanasonicCameraCaptureControl? = null
    private var zoomControl: PanasonicCameraZoomLensControl? = null
    private var cameraConnection: PanasonicCameraConnection? = null
    private val buttonControl = PanasonicButtonControl()
    private val runMode = PanasonicRunMode()

    private val hardwareStatus = PanasonicHardwareStatus()
    private val statusHolder = PanasonicStatus()
    private val playbackControl = PanasonicPlaybackControl(context, informationReceiver)

    override fun prepare()
    {
        Log.v(TAG, "PanasonicCameraWrapper::prepare() : ${panasonicCamera?.getFriendlyName()} ${panasonicCamera?.getModelName()}")
        try
        {
            if (panasonicCamera != null)
            {
                runMode.setCamera(panasonicCamera, playbackControl, TIMEOUT_MS)
                playbackControl.setCamera(panasonicCamera!!, TIMEOUT_MS)
                focusControl?.setCamera(panasonicCamera!!)
                captureControl?.setCamera(panasonicCamera!!)
                zoomControl?.setCamera(panasonicCamera!!)
                if (eventObserver == null)
                {
                    eventObserver = CameraEventObserver.newInstance(context, panasonicCamera!!, cardSlotSelector)
                }
                if (liveViewControl == null) {
                    liveViewControl = PanasonicLiveViewControl(panasonicCamera!!)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startRecMode()
    {
        try
        {
            // 撮影モード(RecMode)に切り替え
            val sessionId = panasonicCamera?.getCommunicationSessionId()
            val urlToSend = "${panasonicCamera?.getCmdUrl()}cam.cgi?mode=camcmd&value=recmode"
            val reply1 = if (!sessionId.isNullOrEmpty())
            {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["X-SESSION_ID"] = sessionId
                SimpleHttpClient.httpGetWithHeader(urlToSend, headerMap, null, TIMEOUT_MS)
            }
            else
            {
                SimpleHttpClient.httpGet(urlToSend, TIMEOUT_MS)
            }
            if (!reply1.contains("ok"))
            {
                Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE RECMODE.")
            }

            //  フォーカスに関しては、１点に切り替える（仮）
            val urlToSendFocus = "${panasonicCamera?.getCmdUrl()}cam.cgi?mode=setsetting&type=afmode&value=1area"
            val reply2 = if (!sessionId.isNullOrEmpty())
            {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["X-SESSION_ID"] = sessionId
                SimpleHttpClient.httpGetWithHeader(urlToSendFocus, headerMap, null, TIMEOUT_MS)
            }
            else
            {
                SimpleHttpClient.httpGet(urlToSendFocus, TIMEOUT_MS)
            }
            if (!reply2.contains("ok"))
            {
                Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE AF MODE 1area.")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startPlayMode()
    {
        try
        {
            // 参照モード(PlayMode)に切り替え
            val sessionId = panasonicCamera?.getCommunicationSessionId()
            val urlToSend = "${panasonicCamera?.getCmdUrl()}cam.cgi?mode=camcmd&value=playmode"
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
                Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE PLAYMODE.  $reply")
            }

            // 一覧取得の準備を行う
            playbackControl.preprocessPlaymode()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startEventWatch(listener: ICameraChangeListener?)
    {
        Log.v(TAG, " startEventWatch ")
        try
        {
            if (eventObserver != null)
            {
                if (listener != null)
                {
                    eventObserver?.setEventListener(listener)
                }
                eventObserver?.activate()
                eventObserver?.start()
                val holder = eventObserver?.getCameraStatusHolder()
                holder?.getLiveviewStatus()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun detectedCamera(camera: IPanasonicCamera?)
    {
        Log.v(TAG, "detectedCamera() : " + camera?.getModelName() + " " + camera?.getFriendlyName())
        panasonicCamera = camera
    }

    override fun getPanasonicCameraConnection(): ICameraConnection?
    {
        // PanasonicCameraConnectionは複数生成しない。
        if (cameraConnection == null) {
            cameraConnection = PanasonicCameraConnection(
                context, provider, this,
                listener
            )
        }
        return (cameraConnection)
    }

    override fun getPanasonicLiveViewControl(): ILiveViewControl?
    {
        return (liveViewControl)
    }

    override fun getLiveViewListener(): ILiveViewListener?
    {
        return (liveViewControl?.getLiveViewListener())
    }
    override fun getButtonControl(): ICameraButtonControl
    {
        return (buttonControl)
    }

    override fun getPlaybackControl(): IPlaybackControl
    {
        return (playbackControl)
    }

    override fun getHardwareStatus(): ICameraHardwareStatus
    {
        return (hardwareStatus)
    }

    override fun getCameraRunMode(): ICameraRunMode
    {
        return (runMode)
    }

    override fun getFocusingControl(): IFocusingControl?
    {
        return (focusControl)
    }

    override fun getCameraInformation(): ICameraInformation?
    {
        return (null)
    }

    override fun getZoomLensControl(): IZoomLensControl?
    {
        return (zoomControl)
    }

    override fun getCaptureControl(): ICaptureControl?
    {
        return (captureControl)
    }

    override fun getDisplayInjector(): IDisplayInjector
    {
        return (this)
    }

    override fun getStatusListener(): ICameraStatusUpdateNotify
    {
        return (statusHolder)
    }

    override fun getCameraStatusWatcher(): ICameraStatusWatcher
    {
        return (statusHolder)
    }

    override fun getCameraStatusListHolder(): ICameraStatus
    {
        return (statusHolder)
    }

    override fun getPanasonicCamera(): IPanasonicCamera?
    {
        return (panasonicCamera)
    }

    override fun injectDisplay(frameDisplayer: IAutoFocusFrameDisplay, indicator: IIndicatorControl, focusingModeNotify: IFocusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()")

        focusControl = PanasonicCameraFocusControl(frameDisplayer, indicator)
        captureControl = PanasonicCameraCaptureControl(frameDisplayer, indicator)
        zoomControl = PanasonicCameraZoomLensControl()
    }

    companion object
    {
        private const val TIMEOUT_MS = 5000
        private val TAG = PanasonicCameraWrapper::class.java.simpleName
    }
}
