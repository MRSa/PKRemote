package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.connection

import android.app.Activity
import android.util.Log
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCameraHolder

/**
 * Panasonicカメラとの接続処理
 */
class PanasonicCameraConnectSequence internal constructor(
    private val context: Activity,
    private val cameraStatusReceiver: ICameraStatusReceiver,
    private val cameraConnection: ICameraConnection,
    private val cameraHolder: IPanasonicCameraHolder,
    private val listener: ICameraChangeListener
) : Runnable, PanasonicSsdpClient.ISearchResultCallback
{
    private val client: PanasonicSsdpClient

    init
    {
        Log.v(TAG, "PanasonicCameraConnectSequence")
        client = PanasonicSsdpClient(context, this, cameraStatusReceiver, 1)
    }

    override fun run()
    {
        Log.v(TAG, "search()")
        try
        {
            cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start))
            client.search()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onDeviceFound(cameraDevice: IPanasonicCamera?)
    {
        try
        {
            Log.v(TAG, "onDeviceFound() : ${cameraDevice?.getFriendlyName()}")
            cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_detected) + " ${cameraDevice?.getFriendlyName()}")
            cameraHolder.detectedCamera(cameraDevice)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onFinished()
    {
        Log.v(TAG, "PanasonicCameraConnectSequence.onFinished()")
        try
        {
            val thread = Thread {
                try
                {
                    cameraHolder.prepare()
                    //cameraHolder.startRecMode()
                    cameraHolder.startPlayMode()
                    cameraHolder.startEventWatch(listener)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
                Log.v(TAG, "CameraConnectSequence:: connected.")
                onConnectNotify()
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun onConnectNotify()
    {
        try
        {
            val thread = Thread {
                // カメラとの接続確立を通知する
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected))
                cameraStatusReceiver.onCameraConnected()
                Log.v(TAG, "onConnectNotify()")
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onErrorFinished(reason: String?)
    {
        cameraConnection.alertConnectingFailed(reason)
    }

    companion object
    {
        private val TAG = PanasonicCameraConnectSequence::class.java.simpleName
    }
}
