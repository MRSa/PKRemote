package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper

import androidx.appcompat.app.AppCompatActivity
import net.osdn.gokigen.pkremote.IInformationReceiver
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl
import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IDisplayInjector
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraHardwareStatus
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraInformation
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher
import net.osdn.gokigen.pkremote.camera.vendor.visionkids.IVisionKidsInterfaceProvider
import net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.connection.VisionKidsConnection
import net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.playback.VisionKidsPlaybackControl

class VisionKidsInterfaceProvider(activity: AppCompatActivity, provider: ICameraStatusReceiver, informationReceiver: IInformationReceiver) : IVisionKidsInterfaceProvider, ICameraRunMode
{
    private val playbackControl = VisionKidsPlaybackControl(activity, provider, informationReceiver)
    private val cameraConnection = VisionKidsConnection(activity, provider)
    private val hardwareStatus = VisionKidsHardwareStatus()

    // IVisionKidsInterfaceProvider
    override fun getVisionKidsCameraConnection(): ICameraConnection
    {
        return (cameraConnection)
    }

    // IVisionKidsInterfaceProvider
    override fun getLiveViewControl(): ILiveViewControl?
    {
        return (null)
    }

    // IVisionKidsInterfaceProvider
    override fun getLiveViewListener(): ILiveViewListener?
    {
        return (null)
    }

    // IVisionKidsInterfaceProvider
    override fun getFocusingControl(): IFocusingControl?
    {
        return (null)
    }

    // IVisionKidsInterfaceProvider
    override fun getCameraInformation(): ICameraInformation?
    {
        return (null)
    }

    // IVisionKidsInterfaceProvider
    override fun getZoomLensControl(): IZoomLensControl?
    {
        return (null)
    }

    // IVisionKidsInterfaceProvider
    override fun getCaptureControl(): ICaptureControl?
    {
        return (null)
    }

    // IVisionKidsInterfaceProvider
    override fun getDisplayInjector(): IDisplayInjector?
    {
        return (null)
    }

    // IVisionKidsInterfaceProvider
    override fun getCameraStatusListHolder(): ICameraStatus?
    {
        return (null)
    }

    // IVisionKidsInterfaceProvider
    override fun getButtonControl(): ICameraButtonControl?
    {
        return (null)
    }

    // IVisionKidsInterfaceProvider
    override fun getCameraStatusWatcher(): ICameraStatusWatcher?
    {
        return (null)
    }

    // IVisionKidsInterfaceProvider
    override fun getPlaybackControl(): IPlaybackControl
    {
        return (playbackControl)
    }

    // IVisionKidsInterfaceProvider
    override fun getHardwareStatus(): ICameraHardwareStatus
    {
        return (hardwareStatus)
    }

    // IVisionKidsInterfaceProvider
    override fun getCameraRunMode(): ICameraRunMode
    {
        return (this)
    }

    // ICameraRunMode
    override fun changeRunMode(isRecording: Boolean) { }

    // ICameraRunMode
    override fun isRecordingMode(): Boolean { return (false) }

}
