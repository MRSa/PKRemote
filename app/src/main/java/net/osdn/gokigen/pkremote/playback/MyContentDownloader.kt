package net.osdn.gokigen.pkremote.playback

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IProgressEvent
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor

class MyContentDownloader(
    private val activity: Activity,
    private val playbackControl: IPlaybackControl,
    private val receiver: IContentDownloadNotify?
) : IDownloadContentCallback {

    private var downloadDialog: AlertDialog? = null
    private var progressBar: ProgressBar? = null
    private var textProgressPercent: TextView? = null

    @Volatile
    private var outputStream: OutputStream? = null

    @Volatile
    private var targetFileName = ""

    @Volatile
    private var mimeType = "image/jpeg"

    @Volatile
    private var isDownloading = false

    @Volatile
    private var imageUri: Uri? = null

    private fun getExternalOutputDirectory(): File
    {
        val directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path +
                File.separator + activity.getString(R.string.app_name2) + File.separator
        val target = File(directoryPath)
        try
        {
            if (!target.exists())
            {
                target.mkdirs()
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Failed to create directory: $directoryPath", e)
        }
        Log.v(TAG, "  ----- RECORD Directory PATH : $directoryPath -----")
        return target
    }

    // --- ダウンロードの開始
    fun startDownload(
        fileInfo: ICameraContent?,
        appendTitle: String,
        replaceJpegSuffix: String?,
        requestSmallSize: Boolean
    ) {
        if (fileInfo == null)
        {
            Log.v(TAG, "startDownload() ICameraContent is NULL...")
            return
        }

        if (isDownloading)
        {
            Log.w(TAG, "Download is already in progress. : ${fileInfo.contentName}")
            return
        }

        try
        {
            isDownloading = true
            var contentFileName = fileInfo.contentName.uppercase(Locale.US)
            if (replaceJpegSuffix != null)
            {
                contentFileName = contentFileName.replace(JPEG_SUFFIX, replaceJpegSuffix)
                targetFileName = contentFileName
            }
            else
            {
                targetFileName = fileInfo.originalName.uppercase(Locale.US)
            }
            Log.v(TAG, "startDownload() $targetFileName")

            var isSmallSize = requestSmallSize
            var isVideo = false

            // MIME Type と拡張子の判定
            when {
                contentFileName.endsWith(RAW_SUFFIX_1) -> { mimeType = "image/x-adobe-dng"; isSmallSize = false }
                contentFileName.endsWith(RAW_SUFFIX_2) -> { mimeType = "image/x-olympus-orf"; isSmallSize = false }
                contentFileName.endsWith(RAW_SUFFIX_3) -> { mimeType = "image/x-pentax-pef"; isSmallSize = false }
                contentFileName.endsWith(RAW_SUFFIX_4) || contentFileName.endsWith(RAW_SUFFIX_A) -> {
                    mimeType = if (contentFileName.endsWith(RAW_SUFFIX_A)) "image/x-panasonic-raw" else "image/x-panasonic-rw2"
                    isSmallSize = false
                }
                contentFileName.endsWith(RAW_SUFFIX_5) -> { mimeType = "image/x-sony-arw"; isSmallSize = false }
                contentFileName.endsWith(RAW_SUFFIX_6) -> { mimeType = "image/x-canon-crw"; isSmallSize = false }
                contentFileName.endsWith(RAW_SUFFIX_7) -> { mimeType = "image/x-canon-cr2"; isSmallSize = false }
                contentFileName.endsWith(RAW_SUFFIX_8) -> { mimeType = "image/x-canon-cr3"; isSmallSize = false }
                contentFileName.endsWith(RAW_SUFFIX_9) -> { mimeType = "image/x-nikon-nef"; isSmallSize = false }
                contentFileName.endsWith(RAW_SUFFIX_0) -> { mimeType = "image/x-fuji-raf"; isSmallSize = false }
                contentFileName.endsWith(MOVIE_SUFFIX) || contentFileName.endsWith(MOVIE_SUFFIX_MP4) -> {
                    mimeType = "video/mp4"
                    isSmallSize = false
                    isVideo = true
                }
                else -> {
                    mimeType = "image/jpeg"
                }
            }

            // ProgressBar を使用したダイアログの表示
            activity.runOnUiThread {
                val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_download_progress, null)
                progressBar = dialogView.findViewById(R.id.progressBar)
                textProgressPercent = dialogView.findViewById(R.id.textProgressPercent)

                progressBar?.progress = 0
                textProgressPercent?.text = "0%"

                val title = activity.getString(R.string.dialog_download_file_title) + appendTitle
                val message = activity.getString(R.string.dialog_download_message) + " " + targetFileName

                downloadDialog = AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()

                downloadDialog?.show()
            }

            val resolver = activity.contentResolver
            val relativePath = Environment.DIRECTORY_DCIM + File.separator + activity.getString(R.string.app_name2)
            val timeStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Calendar.getInstance().time)

            // 拡張子の分離
            val baseName = targetFileName.substringBeforeLast('.', targetFileName)
            val ext = if (targetFileName.contains('.')) "." + targetFileName.substringAfterLast('.') else ""
            val outputFileName = "${baseName}_${timeStamp}${ext}"

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.TITLE, outputFileName)
                put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            }

            val extStorageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                values.put(MediaStore.MediaColumns.IS_PENDING, 1)
                if (isVideo) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
            } else {
                val fullPath = File(getExternalOutputDirectory(), outputFileName).absolutePath
                values.put(MediaStore.MediaColumns.DATA, fullPath)
                if (isVideo) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            }

            imageUri = resolver.insert(extStorageUri, values)
            val uri = imageUri

            if (uri != null)
            {
                try
                {
                    outputStream = resolver.openOutputStream(uri)
                    val path = fileInfo.contentPath + "/" + contentFileName
                    Log.v(TAG, "downloadContent : $path (small: $isSmallSize)")
                    playbackControl.downloadContent(path, isSmallSize, this)
                }
                catch (e: Exception)
                {
                    Log.e(TAG, "Failed to open output stream or start download", e)
                    cleanupFailedDownload()
                    activity.runOnUiThread {
                        dismissDialogInternal()
                        presentMessage(activity.getString(R.string.download_control_save_failed), e.message)
                    }
                }
            }
            else
            {
                Log.e(TAG, "Failed to create MediaStore entry.")
                cleanupFailedDownload()
                dismiss()
            }
        }
        catch (t: Throwable)
        {
            Log.e(TAG, "Error in startDownload", t)
            cleanupFailedDownload()
            dismiss()
        }
    }

    private fun dismiss()
    {
        activity.runOnUiThread {
            dismissDialogInternal()
        }
    }

    private fun dismissDialogInternal()
    {
        try
        {
            if (downloadDialog?.isShowing == true) {
                downloadDialog?.dismiss()
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Error dismissing dialog", e)
        }
        finally
        {
            downloadDialog = null
            progressBar = null
            textProgressPercent = null
            isDownloading = false
        }
    }

    override fun onProgress(bytes: ByteArray?, length: Int, progressEvent: IProgressEvent)
    {
        try
        {
            val percent = (progressEvent.progress * 100.0f).toInt()

            // UI スレッドで ProgressBar の進捗・テキストを更新
            activity.runOnUiThread {
                progressBar?.progress = percent
                textProgressPercent?.text = activity.getString(R.string.download_progress_percent, percent)
            }

            if (outputStream != null && bytes != null && length > 0)
            {
                outputStream?.write(bytes, 0, length)
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Error during onProgress write", e)
        }
    }

    override fun onCompleted()
    {
        try
        {
            outputStream?.flush()
            outputStream?.close()
            outputStream = null

            val uri = imageUri
            if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                activity.contentResolver.update(uri, values, null, null)
            }

            if (uri != null)
            {
                activity.runOnUiThread {
                    val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
                    if (preferences.getBoolean(IPreferencePropertyAccessor.SHARE_AFTER_SAVE, false))
                    {
                        shareContent(uri, mimeType)
                    }
                    try
                    {
                        receiver?.downloadedImage(targetFileName, uri)
                    }
                    catch (e: Exception)
                    {
                        Log.e(TAG, "Receiver callback failed", e)
                    }
                }
            }

            activity.runOnUiThread {
                dismissDialogInternal()
                val view = activity.findViewById<View>(R.id.fragment1)
                if (view != null)
                {
                    Snackbar.make(
                        view,
                        activity.getString(R.string.download_control_save_success) + " " + targetFileName,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Error onCompleted", e)
            cleanupFailedDownload()
            activity.runOnUiThread {
                dismissDialogInternal()
                presentMessage(activity.getString(R.string.download_control_save_failed), e.message)
            }
        }
    }

    override fun onErrorOccurred(e: Exception)
    {
        Log.e(TAG, "onErrorOccurred", e)
        val message = e.message

        cleanupFailedDownload()

        activity.runOnUiThread {
            dismissDialogInternal()
            presentMessage(activity.getString(R.string.download_control_download_failed), message)
        }
    }

    private fun cleanupFailedDownload()
    {
        try
        {
            outputStream?.flush()
            outputStream?.close()
        }
        catch (ex: Exception)
        {
            Log.e(TAG, "Failed to close output stream on cleanup", ex)
        }
        finally
        {
            outputStream = null
        }

        imageUri?.let { uri ->
            try
            {
                activity.contentResolver.delete(uri, null, null)
            }
            catch (ex: Exception)
            {
                Log.e(TAG, "Failed to delete pending/incomplete file: $uri", ex)
            }
            imageUri = null
        }
    }

    fun isDownloading(): Boolean
    {
        return isDownloading
    }

    private fun shareContent(fileUri: Uri?, contentType: String)
    {
        if (fileUri == null) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = contentType
            putExtra(Intent.EXTRA_STREAM, fileUri)
        }
        try
        {
            activity.startActivityForResult(intent, 0)
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Failed to start share activity", e)
        }
    }

    private fun presentMessage(title: String, message: String?)
    {
        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    companion object {
        private const val TAG = "MyContentDownloader"
        private const val RAW_SUFFIX_1 = ".DNG"
        private const val RAW_SUFFIX_2 = ".ORF"
        private const val RAW_SUFFIX_3 = ".PEF"
        private const val RAW_SUFFIX_4 = ".RW2"
        private const val RAW_SUFFIX_5 = ".ARW"
        private const val RAW_SUFFIX_6 = ".CRW"
        private const val RAW_SUFFIX_7 = ".CR2"
        private const val RAW_SUFFIX_8 = ".CR3"
        private const val RAW_SUFFIX_9 = ".NEF"
        private const val RAW_SUFFIX_0 = ".RAF"
        private const val RAW_SUFFIX_A = ".RAW"
        private const val MOVIE_SUFFIX = ".MOV"
        private const val MOVIE_SUFFIX_MP4 = ".MP4"
        private const val JPEG_SUFFIX = ".JPG"
    }
}
