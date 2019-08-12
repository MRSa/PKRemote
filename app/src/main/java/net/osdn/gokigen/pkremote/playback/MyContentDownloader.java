package net.osdn.gokigen.pkremote.playback;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IProgressEvent;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

/**
 *   コンテントのダウンロード
 *
 */
public class MyContentDownloader implements IDownloadContentCallback
{
    private final String TAG = toString();
    private final Activity activity;
    private final IPlaybackControl playbackControl;
    private final IContentDownloadNotify receiver;
    private static final String RAW_SUFFIX_1 = ".DNG";
    private static final String RAW_SUFFIX_2 = ".ORF";
    private static final String RAW_SUFFIX_3 = ".PEF";
    private static final String RAW_SUFFIX_4 = ".RW2";
    private static final String MOVIE_SUFFIX = ".MOV";
    private static final String MOVIE_SUFFIX_MP4 = ".MP4";
    private static final String JPEG_SUFFIX = ".JPG";
    private ProgressDialog downloadDialog = null;
    private FileOutputStream outputStream = null;
    private String targetFileName = "";
    private String filepath = "";
    private String mimeType = "image/jpeg";
    private boolean isDownloading = false;

    /**
     *   コンストラクタ
     *
     */
    public MyContentDownloader(@NonNull Activity activity, @NonNull final IPlaybackControl playbackControl, @Nullable IContentDownloadNotify receiver)
    {
        this.activity = activity;
        this.playbackControl = playbackControl;
        this.receiver = receiver;
    }

    /**
     *   ダウンロードの開始
     *
     */
    public void startDownload(final ICameraContent fileInfo, final String appendTitle, String replaceJpegSuffix, boolean isSmallSize)
    {
        if (fileInfo == null)
        {
            Log.v(TAG, "startDownload() ICameraFileInfo is NULL...");
            return;
        }
        Log.v(TAG, "startDownload() " + fileInfo.getContentName());

        // Download the image.
        try
        {
            isDownloading = true;
            Calendar calendar = Calendar.getInstance();
            String extendName = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(calendar.getTime());
            targetFileName = fileInfo.getContentName().toUpperCase();
            if (replaceJpegSuffix != null)
            {
                targetFileName = targetFileName.replace(JPEG_SUFFIX, replaceJpegSuffix);
            }
            if (targetFileName.toUpperCase().contains(RAW_SUFFIX_1))
            {
                mimeType = "image/x-adobe-dng";
                isSmallSize = false;
            }
            else if (targetFileName.toUpperCase().contains(RAW_SUFFIX_2))
            {
                mimeType = "image/x-olympus-orf";
                isSmallSize = false;
            }
            else if (targetFileName.toUpperCase().contains(RAW_SUFFIX_3))
            {
                mimeType = "image/x-pentax-pef";
                isSmallSize = false;
            }
            else if (targetFileName.toUpperCase().contains(RAW_SUFFIX_4))
            {
                mimeType = "image/x-panasonic-raw2";
                isSmallSize = false;
            }
            else if (targetFileName.toUpperCase().contains(MOVIE_SUFFIX))
            {
                mimeType =  "video/mp4";
                isSmallSize = false;
            }
            else if (targetFileName.toUpperCase().contains(MOVIE_SUFFIX_MP4))
            {
                mimeType =  "video/mp4";
                isSmallSize = false;
            }
            else
            {
                mimeType = "image/jpeg";
            }

            ////// ダイアログの表示
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    if (downloadDialog == null)
                    {
                        downloadDialog = new ProgressDialog(activity);
                    }
                    downloadDialog.setTitle(activity.getString(R.string.dialog_download_file_title) + appendTitle);
                    downloadDialog.setMessage(activity.getString(R.string.dialog_download_message) + " " + targetFileName);
                    downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    downloadDialog.setCancelable(false);
                    downloadDialog.show();
                }
            });
            String path = fileInfo.getContentPath() + "/" + targetFileName;

            final String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/" + activity.getString(R.string.app_name2) + "/";
            String outputFileName =  extendName + "_" + targetFileName;
            filepath = new File(directoryPath.toLowerCase(), outputFileName.toLowerCase()).getPath();
            try
            {
                final File directory = new File(directoryPath);
                if (!directory.exists())
                {
                    if (!directory.mkdirs())
                    {
                        Log.v(TAG, "MKDIR FAIL. : " + directoryPath);
                    }
                }
                outputStream = new FileOutputStream(filepath);

                Log.v(TAG, "downloadContent : " + path + " (small: " + isSmallSize + ")");
                playbackControl.downloadContent(path, isSmallSize, this);
            }
            catch (Exception e)
            {
                final String message = e.getMessage();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (downloadDialog != null) {
                            downloadDialog.dismiss();
                        }
                        downloadDialog = null;
                        isDownloading = false;
                        presentMessage(activity.getString(R.string.download_control_save_failed), message);
                    }
                });
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        if (downloadDialog != null) {
                            downloadDialog.dismiss();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    downloadDialog = null;
                    isDownloading = false;
                }
            });
        }
    }

    @Override
    public void onProgress(byte[] bytes, int length, IProgressEvent progressEvent)
    {
        if (downloadDialog != null)
        {
            int percent = (int)(progressEvent.getProgress() * 100.0f);
            downloadDialog.setProgress(percent);
            //downloadDialog.setCancelable(progressEvent.isCancellable()); // キャンセルできるようにしないほうが良さそうなので
            //Log.v(TAG, "DOWNLOAD (" + percent + "%) " + bytes.length);
        }
        try
        {
            if ((outputStream != null)&&(length > 0))
            {
                outputStream.write(bytes, 0, length);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompleted()
    {
        try
        {
            if (outputStream != null)
            {
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            }
            if ((!targetFileName.toUpperCase().endsWith(RAW_SUFFIX_1))&&(!targetFileName.toUpperCase().endsWith(RAW_SUFFIX_2))&&(!targetFileName.toUpperCase().endsWith(RAW_SUFFIX_3)))
            {
                // ギャラリーに受信したファイルを登録する
                long now = System.currentTimeMillis();
                ContentValues values = new ContentValues();
                ContentResolver resolver = activity.getContentResolver();
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Images.Media.DATA, filepath);
                values.put(MediaStore.Images.Media.DATE_ADDED, now);
                values.put(MediaStore.Images.Media.DATE_TAKEN, now);
                values.put(MediaStore.Images.Media.DATE_MODIFIED, now);
                Uri mediaValue = mimeType.contains("video") ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                final Uri content = resolver.insert(mediaValue, values);
                try
                {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                                if (preferences.getBoolean(IPreferencePropertyAccessor.SHARE_AFTER_SAVE, false))
                                {
                                    shareContent(content, mimeType);
                                }
                                try
                                {
                                    if (receiver != null)
                                    {
                                        receiver.downloadedImage(targetFileName, content);
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    if (downloadDialog != null)
                    {
                        downloadDialog.dismiss();
                    }
                    downloadDialog = null;
                    isDownloading = false;
                    View view = activity.findViewById(R.id.fragment1);
                    Snackbar.make(view, activity.getString(R.string.download_control_save_success) + " " + targetFileName, Snackbar.LENGTH_SHORT).show();
                    //Toast.makeText(activity, activity.getString(R.string.download_control_save_success) + " " + targetFileName, Toast.LENGTH_SHORT).show();
                    System.gc();
                }
            });
        }
        catch (Exception e)
        {
            final String message = e.getMessage();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (downloadDialog != null)
                    {
                        downloadDialog.dismiss();
                    }
                    downloadDialog = null;
                    isDownloading = false;
                    presentMessage(activity.getString(R.string.download_control_save_failed), message);
                }
            });
        }
        System.gc();
    }

    @Override
    public void onErrorOccurred(Exception e)
    {
        isDownloading = false;
        final String message = e.getMessage();
        try
        {
            e.printStackTrace();
            if (outputStream != null)
            {
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (downloadDialog != null)
                {
                    downloadDialog.dismiss();
                }
                downloadDialog = null;
                isDownloading = false;
                presentMessage(activity.getString(R.string.download_control_download_failed), message);
            }
        });
        System.gc();
    }

    public boolean isDownloading()
    {
        return (isDownloading);
    }

    /**
     *   共有の呼び出し
     *
     * @param fileUri  ファイルUri
     */
    private void shareContent(final Uri fileUri, final String contentType)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        try
        {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType(contentType);   // "video/mp4"  or "image/jpeg"  or "image/x-adobe-dng"
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            activity.startActivityForResult(intent, 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void presentMessage(final String title, final String message)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(title).setMessage(message);
                builder.show();
            }
        });
    }
}
