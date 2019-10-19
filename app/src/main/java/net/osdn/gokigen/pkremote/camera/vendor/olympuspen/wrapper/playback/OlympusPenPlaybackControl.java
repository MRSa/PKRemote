package net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper.playback;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.playback.ProgressEvent;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 *
 *
 */
public class OlympusPenPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private static final int DEFAULT_TIMEOUT = 3000;
    private final Activity activity;
    private final int timeoutValue;
    private OlympusPenObjectDataHolder imageListHolder = new OlympusPenObjectDataHolder();

    public OlympusPenPlaybackControl(@NonNull Activity activity, int timeoutMs)
    {
        Log.v(TAG, "OlympusPenPlaybackControl()");
        this.activity = activity;
        this.timeoutValue  = (timeoutMs < DEFAULT_TIMEOUT) ? DEFAULT_TIMEOUT : timeoutMs;
    }

    @Override
    public String getRawFileSuffix()
    {
        return (".ORF");
    }

    @Override
    public void downloadContentList(@NonNull IDownloadContentListCallback callback)
    {
        Log.v(TAG, " downloadContentList()");
    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info)
    {
        Log.v(TAG, " updateCameraFileInfo() : " + info.getFilename());
    }

    @Override
    public void getContentInfo(@NonNull String path, @NonNull String name, @NonNull IContentInfoCallback callback)
    {
        Log.v(TAG, " getContentInfo() : " + path + " / " + name);

        //　画像の情報を取得する
    }

    @Override
    public void downloadContentScreennail(@NonNull String path, @NonNull IDownloadThumbnailImageCallback callback)
    {
        Log.v(TAG, "downloadContentScreennail() : " + path);
        try
        {
            String url = "http://192.168.0.10/get_screennail.cgi?DIR=" + path;

            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
            headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

            Bitmap bmp = SimpleHttpClient.httpGetBitmap(url, headerMap, timeoutValue);
            if (bmp != null)
            {
                HashMap<String, Object> map = new HashMap<>();
                map.put("Orientation", 0);
                callback.onCompleted(bmp, map);
                return;
            }

            // screennail取得失敗時...リカバリする
            try
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                boolean smallSize = preferences.getBoolean(IPreferencePropertyAccessor.OLYMPUS_USE_SCREENNAIL_AS_SMALL, false);
                if (smallSize)
                {
                    // 小さい画像をscreennailとして利用する
                    url = "http://192.168.0.10/get_resizeimg.cgi?DIR=" + path + "&size=1024";
                    bmp = SimpleHttpClient.httpGetBitmap(url, headerMap, timeoutValue);
                    if (bmp != null)
                    {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("Orientation", 0);
                        callback.onCompleted(bmp, map);
                        return;
                    }
                    // それでもダメな場合はサムネイル画像を使う...
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


            // サムネイルでscreennail表示...
            downloadContentThumbnail(path, callback);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            callback.onErrorOccurred(new NullPointerException());
        }
    }

    @Override
    public void downloadContentThumbnail(@NonNull String path, @NonNull IDownloadThumbnailImageCallback callback)
    {
        Log.v(TAG, "downloadContentThumbnail() : " + path);
        try
        {
            String url = "http://192.168.0.10/get_thumbnail.cgi?DIR=" + path;

            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
            headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"
            Bitmap bmp = SimpleHttpClient.httpGetBitmap(url, headerMap, timeoutValue);
            HashMap<String, Object> map = new HashMap<>();
            map.put("Orientation", 0);
            callback.onCompleted(bmp, map);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            callback.onErrorOccurred(new NullPointerException());
        }
   }

    @Override
    public void downloadContent(@NonNull String  path, boolean isSmallSize, @NonNull final IDownloadContentCallback callback)
    {
        Log.v(TAG, "downloadContent() : " + path + " (small :" + isSmallSize + ")");
        try
        {
            String url;
            if ((isSmallSize)&&(path.contains(".JPG")))
            {
                String smallSize = "1600";
                try
                {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                    smallSize = preferences.getString(IPreferencePropertyAccessor.PEN_SMALL_PICTURE_SIZE, IPreferencePropertyAccessor.PEN_SMALL_PICTURE_SIZE_DEFAULT_VALUE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                // 縮小サイズで画像をとる
                url = "http://192.168.0.10/get_resizeimg.cgi?DIR=" + path + "&size=" + smallSize;
            }
            else
            {
                url = "http://192.168.0.10/" + path;
            }

            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
            headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

            SimpleHttpClient.httpGetBytes(url, headerMap, timeoutValue, new SimpleHttpClient.IReceivedMessageCallback() {
                @Override
                public void onCompleted() {
                    callback.onCompleted();
                }

                @Override
                public void onErrorOccurred(Exception e) {
                    callback.onErrorOccurred(e);
                }

                @Override
                public void onReceive(int readBytes, int length, int size, byte[] data) {
                    float percent = (length == 0) ? 0.0f : ((float) readBytes / (float) length);
                    //Log.v(TAG, " onReceive : " + readBytes + " " + length + " " + size);
                    ProgressEvent event = new ProgressEvent(percent, null);
                    callback.onProgress(data, size, event);
                }
            });
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   カメラ内画像ファイルの取得処理
     *      - フォルダ一覧を取得してから、それぞれのフォルダ内に入っている画像一覧を取得する
     */
    @Override
    public void getCameraContentList(ICameraContentListCallback callback)
    {
        String imageListTopLevelUrl = "http://192.168.0.10/get_imglist.cgi?DIR=/DCIM";
        String contentInformation;
        try
        {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
            headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

            // フォルダー情報を取得する
            contentInformation = SimpleHttpClient.httpGetWithHeader(imageListTopLevelUrl, headerMap, null, timeoutValue);
            Log.v(TAG, " " + imageListTopLevelUrl + " " + contentInformation);
            imageListHolder.clear();
            for (OlympusPenCameraContent path : imageListHolder.parsePath(contentInformation))
            {
                //  フォルダ内の画像を取得する
                String imageListPathUrl = "http://192.168.0.10/get_imglist.cgi?DIR=/DCIM/" + path.getContentName();
                String imgList = SimpleHttpClient.httpGetWithHeader(imageListPathUrl, headerMap, null, timeoutValue);
                if ((imgList != null)&&(imgList.length() > 0))
                {
                    imageListHolder.parseImage(imgList);
                }
            }
            callback.onCompleted(imageListHolder.getImageList());
        }
        catch (Exception e)
        {
            // 例外をそのまま転送
            callback.onErrorOccurred(e);
        }
    }

    @Override
    public void showPictureStarted()
    {

    }

    @Override
    public void showPictureFinished()
    {

    }
}
