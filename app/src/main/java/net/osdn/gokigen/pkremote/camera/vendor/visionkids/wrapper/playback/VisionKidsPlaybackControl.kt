package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.playback

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import net.osdn.gokigen.pkremote.IInformationReceiver
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver
import net.osdn.gokigen.pkremote.camera.playback.ProgressEvent
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient.IReceivedMessageCallback
import net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.connection.IVisionKidsConnection
import java.util.Date

class VisionKidsPlaybackControl(activity: AppCompatActivity, private val provider: ICameraStatusReceiver, private val informationReceiver: IInformationReceiver, timeoutMs: Int = DEFAULT_TIMEOUT): IPlaybackControl, IVisionKidsConnection
{
    private val contentProvider = VisionKidsCameraContentProvider(activity)
    private val timeoutValue = Math.max(DEFAULT_TIMEOUT, timeoutMs)

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
        try
        {
            if (name == null)
            {
                callback?.onErrorOccurred(Exception())
                return
            }
            val contentInfo = contentProvider.getCameraContent(name)
            if (contentInfo != null)
            {
                callback?.onCompleted(VisionKidsCameraInfo(name, contentInfo.capturedDate))
            }
            else
            {
                callback?.onCompleted(VisionKidsCameraInfo(name, Date()))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return
    }

    override fun updateCameraFileInfo(info: ICameraFileInfo?)
    {
        // 利用箇所なし
    }

    override fun downloadContentScreennail(path: String?, callback: IDownloadThumbnailImageCallback?)
    {
        // サムネイルしか取得できないので統合する
        downloadContentThumbnail(path, callback)
    }

    override fun downloadContentThumbnail(path: String?, callback: IDownloadThumbnailImageCallback?)
    {
        try
        {
            val address = contentProvider.getHostAddress()
            val urlToGet = "http://$address/DCIM/T/$path".replace("//","/")
            Log.v(TAG, "downloadContentThumbnail($path) : $urlToGet")

            val bmp = SimpleHttpClient.httpGetBitmap(urlToGet, HashMap(), timeoutValue)
            val map = HashMap<String, Any>()
            map["Orientation"] = 0
            callback?.onCompleted(bmp, map)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            callback?.onErrorOccurred(e)
        }
    }

    override fun downloadContent(path: String?, isSmallSize: Boolean, callback: IDownloadContentCallback?)
    {
        try
        {
            // 取得先URLを特定する
            val dataType = if (isSmallSize) { "T" } else { "O" }
            val address = contentProvider.getHostAddress()
            val urlToGet = "http://$address/DCIM/$dataType/$path".replace("//","/")
            Log.v(TAG, "downloadContent($path, $isSmallSize) : $urlToGet")

            // 画像データを取得する
            try
            {
                SimpleHttpClient.httpGetBytes(urlToGet, HashMap(), timeoutValue,
                    object : IReceivedMessageCallback {
                        override fun onCompleted() {
                            callback?.onCompleted()
                        }
                        override fun onErrorOccurred(e: java.lang.Exception) {
                            callback?.onErrorOccurred(e)
                        }
                        override fun onReceive(readBytes: Int, length: Int, size: Int, data: ByteArray) {
                            val percent = if (length == 0) 0.0f else readBytes.toFloat() / length.toFloat()
                            val event = ProgressEvent(percent, null)
                            callback?.onProgress(data, size, event)
                        }
                    })
            }
            catch (t: Throwable)
            {
                t.printStackTrace()
                callback?.onErrorOccurred(NullPointerException())
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
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

    override fun forceDisconnect()
    {
        contentProvider.forceDisconnect()
    }
    companion object
    {
        private val TAG = VisionKidsPlaybackControl::class.java.simpleName
        private const val DEFAULT_TIMEOUT = 3000
    }
}
