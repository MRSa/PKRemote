package net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper.playback;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.playback.CameraContentInfo;
import net.osdn.gokigen.pkremote.camera.playback.CameraFileInfo;
import net.osdn.gokigen.pkremote.camera.playback.ProgressEvent;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import jp.co.olympus.camerakit.OLYCamera;
import jp.co.olympus.camerakit.OLYCameraFileInfo;

public class OlyCameraPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private final OLYCamera camera;
    private final Activity activity;
    //private List<OLYCamera> list;

    public OlyCameraPlaybackControl(@NonNull OLYCamera camera, @NonNull Activity activity)
    {
        this.camera = camera;
        this.activity = activity;
    }

    @Override
    public String getRawFileSuffix()
    {
        return (".ORF");
    }

    @Override
    public void downloadContentList(@NonNull final IDownloadContentListCallback callback)
    {
        try
        {
            changeRunModePlayback();
            camera.downloadContentList(new OLYCamera.DownloadContentListCallback() {
                @Override
                public void onCompleted(List<OLYCameraFileInfo> list)
                {
                    List<ICameraFileInfo> list2 = new ArrayList<>();
                    for (OLYCameraFileInfo fileInfo : list)
                    {
                        CameraFileInfo cameraFileInfo = new CameraFileInfo(fileInfo.getDirectoryPath(), fileInfo.getFilename());
                        cameraFileInfo.setDate(fileInfo.getDatetime());
                        list2.add(cameraFileInfo);
                    }
                    callback.onCompleted(list2);
                }

                @Override
                public void onErrorOccurred(Exception e)
                {
                    callback.onErrorOccurred(e);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callback.onErrorOccurred(e);
        }
    }

    @Override
    public void getContentInfo(@NonNull String path, @NonNull String name, @NonNull IContentInfoCallback callback)
    {
        try
        {
            // ここは使っていないから何もしない... 本当は画像をダウンロードして動きたい。
            Log.v(TAG, "getContentInfo() : " + path + "/" + name );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        callback.onErrorOccurred(new NullPointerException());
    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info)
    {
        try
        {
            Log.v(TAG, "updateCameraFileInfo() : " + info.getFilename());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContentScreennail(@NonNull String path, @NonNull final IDownloadThumbnailImageCallback callback)
    {
        try
        {
            changeRunModePlayback();
            camera.downloadContentScreennail(path, new OLYCamera.DownloadImageCallback() {
                @Override
                public void onProgress(OLYCamera.ProgressEvent progressEvent)
                {
                    // なにもしない
                }

                @Override
                public void onCompleted(byte[] bytes, Map<String, Object> map)
                {
                    try
                    {
                        callback.onCompleted(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), map);
                    }
                    catch (Throwable t)
                    {
                        t.printStackTrace();
                        callback.onErrorOccurred(new NullPointerException());
                    }
                }

                @Override
                public void onErrorOccurred(Exception e)
                {
                    callback.onErrorOccurred(e);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callback.onErrorOccurred(e);
        }
    }

    @Override
    public void downloadContentThumbnail(@NonNull String path, @NonNull final IDownloadThumbnailImageCallback callback)
    {
        try
        {
            changeRunModePlayback();
            camera.downloadContentThumbnail(path, new OLYCamera.DownloadImageCallback() {
                @Override
                public void onProgress(OLYCamera.ProgressEvent progressEvent)
                {
                    // なにもしない
                }

                @Override
                public void onCompleted(byte[] bytes, Map<String, Object> map)
                {
                    try
                    {
                        callback.onCompleted(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), map);
                    }
                    catch (Throwable t)
                    {
                        t.printStackTrace();
                        callback.onErrorOccurred(new NullPointerException());
                    }
                }

                @Override
                public void onErrorOccurred(Exception e)
                {
                    callback.onErrorOccurred(e);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callback.onErrorOccurred(e);
        }
    }

    @Override
    public void downloadContent(@NonNull String path, boolean isSmallSize, @NonNull final IDownloadContentCallback callback)
    {
        try
        {
            changeRunModePlayback();

            String downloadPath = path.toLowerCase();
            float imageSize = OLYCamera.IMAGE_RESIZE_NONE;
            if (isSmallSize)
            {
                try
                {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                    String smallSize = preferences.getString(IPreferencePropertyAccessor.SMALL_PICTURE_SIZE, IPreferencePropertyAccessor.SMALL_PICTURE_SIZE_DEFAULT_VALUE);
                    switch (smallSize)
                    {
                        case "1600":
                            imageSize = OLYCamera.IMAGE_RESIZE_1600;
                            break;
                        case "1920":
                            imageSize = OLYCamera.IMAGE_RESIZE_1920;
                            break;
                        case "2048":
                            imageSize = OLYCamera.IMAGE_RESIZE_2048;
                            break;
                        case "1024":
                        default:
                            imageSize = OLYCamera.IMAGE_RESIZE_1024;
                            break;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    imageSize = OLYCamera.IMAGE_RESIZE_1600;
                }
            }
            if (downloadPath.endsWith(".jpg"))
            {
                // JPEGファイルの取得
                downloadJpegContent(downloadPath, imageSize, callback);
            }
            else
            {
                downloadLargeContent(downloadPath, callback);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callback.onErrorOccurred(e);
        }
    }

    private void downloadJpegContent(@NonNull String path, float imageSize, @NonNull final IDownloadContentCallback callback)
    {
        camera.downloadImage(path, imageSize, new OLYCamera.DownloadImageCallback()
        {
            @Override
            public void onProgress(OLYCamera.ProgressEvent progressEvent)
            {
                try
                {
                    callback.onProgress(null, 0, new ProgressEvent(progressEvent.getProgress(), null));
                    //Log.v(TAG, "progressEvent.getProgress() : " + progressEvent.getProgress());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleted(byte[] bytes, Map<String, Object> map)
            {
                try
                {
                    callback.onProgress(bytes, bytes.length, new ProgressEvent(1.0f, null));
                    callback.onCompleted();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorOccurred(Exception e)
            {
                try
                {
                    callback.onErrorOccurred(e);
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }
            }
        });
    }

    private void downloadLargeContent(@NonNull String path, @NonNull final IDownloadContentCallback callback)
    {
        camera.downloadLargeContent(path, new OLYCamera.DownloadLargeContentCallback()
        {
            @Override
            public void onProgress(byte[] bytes, OLYCamera.ProgressEvent progressEvent)
            {
                try
                {
                    callback.onProgress(bytes, bytes.length, new ProgressEvent(progressEvent.getProgress(), null));
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
                    callback.onCompleted();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorOccurred(Exception e)
            {
                try
                {
                    callback.onErrorOccurred(e);
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }
            }
        });
    }



    /**
     *    ことあるごとに、とにかくRunMode を Playbackにする
     *    (たまに外れることがあるので...)
     *
     */
    private void changeRunModePlayback()
    {
        try
        {
            if (camera.getRunMode() != OLYCamera.RunMode.Playback)
            {
                camera.changeRunMode(OLYCamera.RunMode.Playback);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void getCameraContentList(final ICameraContentListCallback callback)
    {
        if (callback == null)
        {
            // 何もせず戻る
            return;
        }
        try
        {
            changeRunModePlayback();
            camera.downloadContentList(new OLYCamera.DownloadContentListCallback()
            {
                @Override
                public void onCompleted(List<OLYCameraFileInfo> list)
                {
                    List<ICameraContent> list2 = new ArrayList<>();
                    for (OLYCameraFileInfo fileInfo : list)
                    {
                        CameraContentInfo contentInfo = new CameraContentInfo("AirA01", "sd1", fileInfo.getDirectoryPath(), fileInfo.getFilename(), fileInfo.getDatetime());
                        list2.add(contentInfo);
                    }
                    callback.onCompleted(list2);
                }

                @Override
                public void onErrorOccurred(Exception e)
                {
                    callback.onErrorOccurred(e);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callback.onErrorOccurred(e);
        }
    }

    @Override
    public void showPictureStarted() {

    }

    @Override
    public void showPictureFinished() {

    }
}
