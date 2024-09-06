package net.osdn.gokigen.pkremote.camera.vendor.panasonic

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl
import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraStatusUpdateNotify
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IDisplayInjector
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraHardwareStatus
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraInformation
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera

interface IPanasonicInterfaceProvider {
    fun getPanasonicCameraConnection(): ICameraConnection?
    fun getPanasonicLiveViewControl(): ILiveViewControl?
    fun getLiveViewListener(): ILiveViewListener?
    fun getFocusingControl(): IFocusingControl?
    fun getCameraInformation(): ICameraInformation?
    fun getZoomLensControl(): IZoomLensControl?
    fun getCaptureControl(): ICaptureControl?
    fun getDisplayInjector(): IDisplayInjector?

    fun getStatusListener(): ICameraStatusUpdateNotify?

    fun getCameraStatusWatcher(): ICameraStatusWatcher?
    fun getCameraStatusListHolder(): ICameraStatus?

    fun getButtonControl(): ICameraButtonControl?

    fun getPlaybackControl(): IPlaybackControl?
    fun getHardwareStatus(): ICameraHardwareStatus?
    fun getCameraRunMode(): ICameraRunMode?

    fun getPanasonicCamera(): IPanasonicCamera?
}