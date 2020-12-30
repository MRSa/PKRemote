package net.osdn.gokigen.pkremote.playback

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.database.DatabaseUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IProgressEvent
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class MyContentDownloader(private val activity : Activity, private val playbackControl : IPlaybackControl, private val receiver : IContentDownloadNotify?) : IDownloadContentCallback
{
    private lateinit var downloadDialog : ProgressDialog //= ProgressDialog(activity)
    private val dumpLog = false
    private var outputStream: OutputStream? = null
    private var targetFileName = ""
    private var filepath = ""
    private var mimeType = "image/jpeg"
    private var isDownloading = false
    private var imageUri : Uri? = null

    private fun getExternalOutputDirectory(): File
    {
        val directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path + "/" + activity.getString(R.string.app_name2) + "/"
        val target = File(directoryPath)
        try
        {
            target.mkdirs()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        Log.v(TAG, "  ----- RECORD Directory PATH : $directoryPath -----")
        return (target)
    }

    private fun isExternalStorageWritable(): Boolean
    {
        return (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
    }

    /**
     * ダウンロードの開始
     *
     */
    fun startDownload(fileInfo: ICameraContent?, appendTitle: String, replaceJpegSuffix: String?, requestSmallSize: Boolean)
    {
        if (fileInfo == null)
        {
            Log.v(TAG, "startDownload() ICameraFileInfo is NULL...")
            return
        }

        // Download the image.
        var isSmallSize = requestSmallSize
        try
        {
            isDownloading = true
            var contentFileName = fileInfo.contentName.toUpperCase(Locale.US)
            if (replaceJpegSuffix != null)
            {
                contentFileName = contentFileName.replace(JPEG_SUFFIX, replaceJpegSuffix)
                targetFileName = contentFileName
            }
            else
            {
                targetFileName = fileInfo.originalName.toUpperCase(Locale.US)
            }
            Log.v(TAG, "startDownload() $targetFileName")
            when {
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_1) -> {
                    mimeType = "image/x-adobe-dng"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_2) -> {
                    mimeType = "image/x-olympus-orf"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_3) -> {
                    mimeType = "image/x-pentax-pef"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_4) -> {
                    mimeType = "image/x-panasonic-rw2"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_A) -> {
                    // Panasonic
                    mimeType = "image/x-panasonic-raw"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_5) -> {
                    mimeType = "image/x-sony-arw"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_6) -> {
                    mimeType = "image/x-canon-crw"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_7) -> {
                    mimeType = "image/x-canon-cr2"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_8) -> {
                    mimeType = "image/x-canon-cr3"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_9) -> {
                    mimeType = "image/x-nikon-nef"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(RAW_SUFFIX_0) -> {
                    mimeType = "image/x-fuji-raf"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(MOVIE_SUFFIX) -> {
                    mimeType = "video/mp4"
                    isSmallSize = false
                }
                contentFileName.toUpperCase(Locale.US).contains(MOVIE_SUFFIX_MP4) -> {
                    mimeType = "video/mp4"
                    isSmallSize = false
                }
                else -> {
                    mimeType = "image/jpeg"
                }
            }

            ////// ダイアログの表示
            activity.runOnUiThread {
                if (!::downloadDialog.isInitialized)
                {
                    downloadDialog = ProgressDialog(activity)
                }
                downloadDialog.setTitle(activity.getString(R.string.dialog_download_file_title) + appendTitle)
                downloadDialog.setMessage(activity.getString(R.string.dialog_download_message) + " " + targetFileName)
                downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                downloadDialog.setCancelable(false)
                downloadDialog.show()
            }
            val resolver = activity.contentResolver
            val directoryPath = Environment.DIRECTORY_DCIM + File.separator + activity.getString(R.string.app_name2)
            val calendar = Calendar.getInstance()
            val extendName = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(calendar.time)
            val periodPosition = targetFileName.indexOf(".")
            val extension = targetFileName.substring(periodPosition)
            val baseFileName = targetFileName.substring(0, periodPosition)
            val outputFileName = baseFileName + "_" + extendName + extension


            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, outputFileName)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, outputFileName)
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            val extStorageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, directoryPath)
                values.put(MediaStore.Images.Media.IS_PENDING, true)
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                values.put(MediaStore.Images.Media.DATA, getExternalOutputDirectory().absolutePath + File.separator + outputFileName)
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            imageUri = resolver.insert(extStorageUri, values)
            if (imageUri != null)
            {
                ////////////////////////////////////////////////////////////////
                if (dumpLog)
                {
                    if (imageUri != null)
                    {
                        val cursor = resolver.query(imageUri!!, null, null, null, null)
                        DatabaseUtils.dumpCursor(cursor)
                        cursor?.close()
                    }
                }
                ////////////////////////////////////////////////////////////////

                try
                {
                    outputStream = resolver.openOutputStream(imageUri!!)
                    val path = fileInfo.contentPath + "/" + contentFileName
                    Log.v(TAG, "downloadContent : $path (small: $isSmallSize)")
                    playbackControl.downloadContent(path, isSmallSize, this)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                    val message = e.message
                    activity.runOnUiThread {
                        downloadDialog.dismiss()
                        isDownloading = false
                        presentMessage(activity.getString(R.string.download_control_save_failed), message)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        {
                            values.put(MediaStore.Images.Media.IS_PENDING, false)
                            if (imageUri != null)
                            {
                                resolver.update(imageUri!!, values, null, null)
                            }
                        }
                    }
                }

            }
        }
        catch (t: Throwable)
        {
            t.printStackTrace()
            dismiss()
        }
    }

    private fun dismiss()
    {
        activity.runOnUiThread {
            try
            {
                downloadDialog.dismiss()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            isDownloading = false
        }
    }

    override fun onProgress(bytes: ByteArray?, length: Int, progressEvent: IProgressEvent)
    {
        try
        {
            val percent = (progressEvent.progress * 100.0f).toInt()
            downloadDialog.progress = percent
            if ((outputStream != null)&&(bytes != null)&&(length > 0))
            {
                outputStream?.write(bytes, 0, length)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCompleted()
    {
        try
        {
            outputStream?.flush()
            outputStream?.close()

            if (imageUri != null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                {
                    val values = ContentValues()
                    val resolver = activity.contentResolver
                    values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    values.put(MediaStore.Images.Media.DATA, filepath)
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    resolver.update(imageUri!!, values, null, null)
                }
            }
            try
            {

                if (imageUri != null)
                {
                    activity.runOnUiThread {
                        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
                        if (preferences.getBoolean(IPreferencePropertyAccessor.SHARE_AFTER_SAVE, false))
                        {
                            shareContent(imageUri, mimeType)
                        }
                        try
                        {
                            receiver?.downloadedImage(targetFileName, imageUri)
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            activity.runOnUiThread {
                downloadDialog.dismiss()
                isDownloading = false
                val view = activity.findViewById<View>(R.id.fragment1)
                Snackbar.make(view, activity.getString(R.string.download_control_save_success) + " " + targetFileName, Snackbar.LENGTH_SHORT).show()
                System.gc()
            }
        }
        catch (e: Exception)
        {
            val message = e.message
            activity.runOnUiThread {
                downloadDialog.dismiss()
                isDownloading = false
                presentMessage(activity.getString(R.string.download_control_save_failed), message)
            }
        }
        System.gc()
    }

    override fun onErrorOccurred(e: Exception)
    {
        isDownloading = false
        val message = e.message
        try
        {
            e.printStackTrace()
            if (outputStream != null)
            {
                outputStream?.flush()
                outputStream?.close()
            }
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
        activity.runOnUiThread {
            downloadDialog.dismiss()
            isDownloading = false
            presentMessage(activity.getString(R.string.download_control_download_failed), message)
        }
        System.gc()
    }

    fun isDownloading(): Boolean
    {
        return isDownloading
    }

    /**
     * 共有の呼び出し
     *
     * @param fileUri  ファイルUri
     */
    private fun shareContent(fileUri: Uri?, contentType: String)
    {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        try
        {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = contentType // "video/mp4"  or "image/jpeg"  or "image/x-adobe-dng"
            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
            activity.startActivityForResult(intent, 0)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun presentMessage(title: String, message: String?)
    {
        activity.runOnUiThread {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title).setMessage(message)
            builder.show()
        }
    }

    companion object
    {
        private val TAG = this.toString()
        private const val RAW_SUFFIX_1 = ".DNG" // RAW: Ricoh / Pentax
        private const val RAW_SUFFIX_2 = ".ORF" // RAW: Olympus
        private const val RAW_SUFFIX_3 = ".PEF" // RAW: Pentax
        private const val RAW_SUFFIX_4 = ".RW2" // RAW: Panasonic
        private const val RAW_SUFFIX_5 = ".ARW" // RAW: Sony
        private const val RAW_SUFFIX_6 = ".CRW" // RAW: Canon
        private const val RAW_SUFFIX_7 = ".CR2" // RAW: Canon
        private const val RAW_SUFFIX_8 = ".CR3" // RAW: Canon
        private const val RAW_SUFFIX_9 = ".NEF" // RAW: Nikon
        private const val RAW_SUFFIX_0 = ".RAF" // RAW: Fuji
        private const val RAW_SUFFIX_A = ".RAW" // RAW: Panasonic
        private const val MOVIE_SUFFIX = ".MOV"
        private const val MOVIE_SUFFIX_MP4 = ".MP4"
        private const val JPEG_SUFFIX = ".JPG"
    }

}