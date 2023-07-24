package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.playback

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import net.osdn.gokigen.pkremote.IInformationReceiver
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver

class VisionKidsPlaybackControl(activity: AppCompatActivity, private val provider: ICameraStatusReceiver, private val informationReceiver: IInformationReceiver): IPlaybackControl
{
    private val contentProvider = VisionKidsCameraContentProvider(activity)

    override fun getRawFileSuffix(): String
    {
        return (".DNG")
    }

    override fun downloadContentList(callback: IDownloadContentListCallback?)
    {
        // 利用箇所なし
    }

    override fun getContentInfo(path: String?, name: String?, callback: IContentInfoCallback?)
    {
        Log.v(TAG, "getContentInfo($path, $name)")
    }

    override fun updateCameraFileInfo(info: ICameraFileInfo?)
    {
        // 利用箇所なし
    }

    override fun downloadContentScreennail(
        path: String?,
        callback: IDownloadThumbnailImageCallback?
    ) {
        Log.v(TAG, "downloadContentScreennail($path)")
    }

    override fun downloadContentThumbnail(
        path: String?,
        callback: IDownloadThumbnailImageCallback?
    ) {
        Log.v(TAG, "downloadContentThumbnail($path)")
    }

    override fun downloadContent(
        path: String?,
        isSmallSize: Boolean,
        callback: IDownloadContentCallback?
    ) {
        Log.v(TAG, "downloadContent($path, $isSmallSize)")
    }

    override fun getCameraContentList(callback: ICameraContentListCallback)
    {
        Log.v(TAG, "getCameraContentList()")
        try
        {
            contentProvider.getContentList(callback)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun showPictureStarted()
    {
        Log.v(TAG, "showPictureStarted()")
    }

    override fun showPictureFinished()
    {
        Log.v(TAG, "showPictureFinished()")
    }
    companion object
    {
        private val TAG = VisionKidsPlaybackControl::class.java.simpleName
    }
}
