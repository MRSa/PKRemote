package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.playback

import android.app.Activity
import android.util.Log
import net.osdn.gokigen.pkremote.IInformationReceiver
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl
import net.osdn.gokigen.pkremote.camera.playback.ProgressEvent
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient.IReceivedMessageCallback
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera
import java.util.ArrayDeque
import java.util.Queue

class PanasonicPlaybackControl(private val activity: Activity, private val informationReceiver: IInformationReceiver) : IPlaybackControl
{
    private lateinit var panasonicCamera: IPanasonicCamera
    private var timeoutMs = 50000
    private var isStarted = false
    private var cameraContentList: StringBuffer? = null
    private val contentList: MutableList<ICameraContent> = ArrayList()
    private var commandQueue: Queue<DownloadScreennailRequest> = ArrayDeque()

    fun setCamera(panasonicCamera: IPanasonicCamera, timeoutMs: Int)
    {
        Log.v(TAG, "setCamera() " + panasonicCamera.getFriendlyName())
        try
        {
            this.panasonicCamera = panasonicCamera
            this.timeoutMs = timeoutMs
            commandQueue.clear()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun getContentList()
    {
        if (!::panasonicCamera.isInitialized)
        {
            // URLが特定できていないため、送信できないので先に進める
            return
        }

        // PLAYモードに切り替える
        val requestUrl = panasonicCamera.getCmdUrl() + "cam.cgi?mode=camcmd&value=playmode"
        val sessionId = panasonicCamera.getCommunicationSessionId()
        val reqPlay = if (!sessionId.isNullOrEmpty())
        {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["X-SESSION_ID"] = sessionId
            SimpleHttpClient.httpGetWithHeader(requestUrl, headerMap, null, this.timeoutMs)
        }
        else
        {
            SimpleHttpClient.httpGet(requestUrl, this.timeoutMs)
        }
        if (!reqPlay.contains("ok"))
        {
            Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE PLAYMODE.")
        }

        ////////////  ある程度の数に区切って送られてくる... 何度か繰り返す必要があるようだ  ////////////
        cameraContentList = StringBuffer()
        var sequenceNumber = 0
        var totalCount = 100000
        var returnedCount = 0
        while (totalCount > returnedCount) {
            Log.v(TAG, "  ===== getContentList() $sequenceNumber =====")
            sequenceNumber++
            val url = panasonicCamera.getObjUrl() + "Server0/CDS_control"
            val postData =
                "<?xml version=\"1.0\" encoding=\"utf-8\" ?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>" +
                        "<u:Browse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:" + sequenceNumber + "\" xmlns:pana=\"urn:schemas-panasonic-com:pana\">" +
                        "<ObjectID>0</ObjectID><BrowseFlag>BrowseDirectChildren</BrowseFlag><Filter>*</Filter><StartingIndex>" + returnedCount + "</StartingIndex><RequestedCount>3500</RequestedCount><SortCriteria></SortCriteria>" +
                        "<pana:X_FromCP>LumixLink2.0</pana:X_FromCP></u:Browse></s:Body></s:Envelope>"

            val header: MutableMap<String, String> = HashMap()
            header.clear()
            header["SOAPACTION"] = "urn:schemas-upnp-org:service:ContentDirectory:$sequenceNumber#Browse"
            if (!sessionId.isNullOrEmpty())
            {
                header["X-SESSION_ID"] = sessionId
            }

            val reply = SimpleHttpClient.httpPostWithHeader(
                url,
                postData,
                header,
                "text/xml; charset=\"utf-8\"",
                timeoutMs
            )
            if (reply.length < 10)
            {
                Log.v(TAG, postData)
                Log.v(TAG, "ContentDirectory is FAILURE. [$sequenceNumber]")
                break
            }
            cameraContentList = cameraContentList?.append(reply)
            val matches = reply.substring(
                reply.indexOf("<TotalMatches>") + 14,
                reply.indexOf("</TotalMatches>")
            )
            try
            {
                totalCount = matches.toInt()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                totalCount = 0
            }

            val returned = reply.substring(
                reply.indexOf("<NumberReturned>") + 16,
                reply.indexOf("</NumberReturned>")
            )
            try
            {
                returnedCount += returned.toInt()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            Log.v(TAG, "  REPLY DATA : ($matches/$totalCount) [$returned/$returnedCount] ${reply.length} bytes"
            )
            informationReceiver.updateMessage(
                activity.getString(R.string.get_image_list) + " " + returnedCount + "/" + totalCount + " ",
                false,
                false,
                0
            )
        }
    }

    fun preprocessPlaymode()
    {
        try
        {
            // PLAYBACKモードに切り替わった直後に実行する処理をここに書く。
            Log.v(TAG, "  preprocessPlaymode() : " + panasonicCamera.getObjUrl())

            // 画像情報を取得
            getContentList()

            // スクリーンネイルを１こづつ取得するように変更
            getScreenNailService()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun getRawFileSuffix(): String
    {
        Log.v(TAG, " getRawFileSuffix()")
        return ("RW2")
    }

    override fun downloadContentList(callback: IDownloadContentListCallback)
    {
        Log.v(TAG, " downloadContentList()")
    }

    override fun getContentInfo(path: String, name: String, callback: IContentInfoCallback)
    {
        Log.v(TAG, " getContentInfo() : $path / $name")

        //　画像の情報を取得する
    }

    override fun updateCameraFileInfo(info: ICameraFileInfo)
    {
        Log.v(TAG, " updateCameraFileInfo() : " + info.filename)
    }

    override fun downloadContentScreennail(path: String, callback: IDownloadThumbnailImageCallback)
    {
        commandQueue.add(DownloadScreennailRequest(path, callback))
    }

    private fun getScreenNailService()
    {
        try
        {
            if (isStarted)
            {
                // すでにスタートしている場合は、スレッドを走らせない
                return
            }
            val thread = Thread {
                while (true)
                {
                    try
                    {
                        val request = commandQueue.poll()
                        if (request != null)
                        {
                            downloadContentScreennailImpl(request.getPath(), request.getCallback())
                        }
                        Thread.sleep(COMMAND_POLL_QUEUE_MS.toLong())
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }
            try
            {
                isStarted = true
                thread.start()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun downloadContentScreennailImpl(srcPath: String, callback: IDownloadThumbnailImageCallback)
    {
        var path = srcPath
        if (path.startsWith("/"))
        {
            path = path.substring(1)
        }
        val requestUrl = panasonicCamera.getPictureUrl() + "DL" + path.substring(2, path.lastIndexOf(".")) + ".JPG"
        Log.v(TAG, " downloadContentScreennailImpl() : $requestUrl  ")
        try
        {
            val sessionId = panasonicCamera.getCommunicationSessionId()
            val bmp = if (!sessionId.isNullOrEmpty())
            {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["X-SESSION_ID"] = sessionId
                SimpleHttpClient.httpGetBitmap(requestUrl, headerMap, timeoutMs)
            }
            else
            {
                SimpleHttpClient.httpGetBitmap(requestUrl, null, timeoutMs)
            }
            val map = HashMap<String, Any>()
            map["Orientation"] = 0
            callback.onCompleted(bmp, map)
        }
        catch (e: Throwable)
        {
            e.printStackTrace()
            callback.onErrorOccurred(NullPointerException())
        }
    }

    override fun downloadContentThumbnail(srcPath: String, callback: IDownloadThumbnailImageCallback)
    {
        var path = srcPath
        if (path.startsWith("/"))
        {
            path = path.substring(1)
        }
        val requestUrl = panasonicCamera.getPictureUrl() + "DT" + path.substring(2, path.lastIndexOf(".")) + ".JPG"
        Log.v(TAG, " downloadContentThumbnail() : $path  [$requestUrl]")
        try
        {
            val sessionId = panasonicCamera.getCommunicationSessionId()
            val bmp = if (!sessionId.isNullOrEmpty())
            {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["X-SESSION_ID"] = sessionId
                SimpleHttpClient.httpGetBitmap(requestUrl, headerMap, timeoutMs)
            }
            else
            {
                SimpleHttpClient.httpGetBitmap(requestUrl, null, timeoutMs)
            }
            val map = HashMap<String, Any>()
            map["Orientation"] = 0
            callback.onCompleted(bmp, map)
        }
        catch (e: Throwable)
        {
            e.printStackTrace()
            callback.onErrorOccurred(NullPointerException())
        }
    }

    override fun downloadContent(srcPath: String, isSmallSize: Boolean, callback: IDownloadContentCallback)
    {
        var path = srcPath
        if (path.startsWith("/"))
        {
            path = path.substring(1)
        }
        var url = panasonicCamera.getPictureUrl() + path
        if (isSmallSize)
        {
            url = panasonicCamera.getPictureUrl() + "DL" + path.substring(2, path.lastIndexOf(".")) + ".JPG"
        }
        Log.v(TAG, "downloadContent()  PATH : $path GET URL : $url  [$isSmallSize]")

        try {
            var headerMap: MutableMap<String, String>? = HashMap()
            val sessionId = panasonicCamera.getCommunicationSessionId()
            if (!sessionId.isNullOrEmpty())
            {
                headerMap?.set("X-SESSION_ID", sessionId)
            }
            else
            {
                headerMap = null
            }
            SimpleHttpClient.httpGetBytes(url, headerMap, timeoutMs, object : IReceivedMessageCallback {
                override fun onCompleted() {
                    callback.onCompleted()
                }

                override fun onErrorOccurred(e: Exception) {
                    callback.onErrorOccurred(e)
                }

                override fun onReceive(readBytes: Int, length: Int, size: Int, data: ByteArray) {
                    val percent =
                        if ((length == 0)) 0.0f else (readBytes.toFloat() / length.toFloat())
                    val event = ProgressEvent(percent, null)
                    callback.onProgress(data, size, event)
                }
            })
        }
        catch (e: Throwable)
        {
            e.printStackTrace()
        }
    }

    override fun getCameraContentList(callback: ICameraContentListCallback)
    {
        Log.v(TAG, "  getCameraContentList()")

        // 画像情報を取得
        getContentList()

        contentList.clear()
        try
        {
            if (cameraContentList == null)
            {
                //何もしないで終了する
                return
            }
            val objectString = cameraContentList.toString()
            val checkUrl = panasonicCamera.getPictureUrl() ?:""
            val maxIndex = objectString.length - checkUrl.length
            var index = 0

            // データを解析してリストを作る
            while ((index >= 0) && (index < maxIndex))
            {
                index = objectString.indexOf(checkUrl, index)
                if (index > 0)
                {
                    val lastIndex = objectString.indexOf("&", index)
                    val picUrl = objectString.substring(index + checkUrl.length, lastIndex)
                    if (picUrl.startsWith("DO"))
                    {
                        // DO(オリジナル), DL(スクリーンネイル?), DT(サムネイル?)
                        //Log.v(TAG, " pic : " + picUrl)
                        val contentInfo = PanasonicImageContentInfo(picUrl)
                        contentList.add(contentInfo)
                    }
                    index = lastIndex
                }
            }
            callback.onCompleted(contentList)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            callback.onErrorOccurred(e)
        }
    }

    override fun showPictureStarted() {
    }

    override fun showPictureFinished() {
    }

    /**
     * スクリーンネイルの取得キューで使用するクラス
     */
    private inner class DownloadScreennailRequest(private val path: String, private val callback: IDownloadThumbnailImageCallback)
    {
        fun getPath(): String
        {
            return (path)
        }

        fun getCallback(): IDownloadThumbnailImageCallback
        {
            return (callback)
        }
    }

    companion object
    {
        private const val COMMAND_POLL_QUEUE_MS = 50
        private val TAG: String = PanasonicPlaybackControl::class.java.simpleName
    }
}