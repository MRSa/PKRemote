package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation

import android.util.Log
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.takepicture.SingleShotControl
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera

class PanasonicCameraCaptureControl(frameDisplayer: IAutoFocusFrameDisplay, indicator: IIndicatorControl) : ICaptureControl
{
    private val singleShotControl = SingleShotControl(frameDisplayer, indicator)

    fun setCamera(panasonicCamera: IPanasonicCamera)
    {
        singleShotControl.setCamera(panasonicCamera)
    }

    /**
     * 撮影する
     *
     */
    override fun doCapture(kind: Int)
    {
        Log.v(TAG, "doCapture() : $kind")
        try
        {
            singleShotControl.singleShot()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG: String = PanasonicCameraCaptureControl::class.java.simpleName
    }
}
