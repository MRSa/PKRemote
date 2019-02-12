package net.osdn.gokigen.pkremote.camera.vendor.ricoh.wrapper;
import android.graphics.Bitmap;
import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.playback.CameraContentInfo;
import net.osdn.gokigen.pkremote.camera.playback.CameraFileInfo;
import net.osdn.gokigen.pkremote.camera.playback.ProgressEvent;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

/**
 *
 *
 */
public class RicohGr2PlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private final String getPhotoUrl = "http://192.168.0.1/v1/photos/";
    private final RicohGr2StatusChecker statusChecker;
    private static final int DEFAULT_TIMEOUT = 5000;

    /*****
         [操作メモ]
            画像の一覧をとる            : http://192.168.0.1/v1/photos?limit=3000
            画像の情報をとる            ： http://192.168.0.1/v1/photos/yyyRICOH/R0000xxx.DNG/info
            サムネール画像をとる(JPEG)  ： http://192.168.0.1/v1/photos/yyyRICOH/R0000xxx.JPG?size=thumb
            サムネール画像をとる(DNG)   ： http://192.168.0.1/v1/photos/yyyRICOH/R0000xxx.DNG?size=view
            サムネール画像をとる(MOV)   ： http://192.168.0.1/v1/photos/yyyRICOH/R0000xxx.MOV?size=view
            デバイス表示用画像をとる     :  http://192.168.0.1/v1/photos/yyyRICOH/R0000xxx.JPG?size=view
            画像(JPEG)をダウンロードする ： http://192.168.0.1/v1/photos/yyyRICOH/R0000xxx.JPG?size=full
            画像(DNG)をダウンロードする  ： http://192.168.0.1/v1/photos/yyyRICOH/R0000xxx.DNG?size=full
            動画をダウンロードする      ： http://192.168.0.1/v1/photos/yyyRICOH/R0000xxx.MOV?size=full
     *****/

    RicohGr2PlaybackControl(RicohGr2StatusChecker statusChecker)
    {
        this.statusChecker = statusChecker;
    }

    @Override
    public String getRawFileSuffix()
    {
        return (".DNG");
    }

    @Override
    public void downloadContentList(@NonNull IDownloadContentListCallback callback)
    {
        List<ICameraFileInfo> fileList = new ArrayList<>();
        String imageListurl = "http://192.168.0.1/v1/photos?limit=3000";
        String contentList;
        try
        {
            contentList = SimpleHttpClient.httpGet(imageListurl, DEFAULT_TIMEOUT);
            if (contentList == null)
            {
                // ぬるぽ発行
                callback.onErrorOccurred(new NullPointerException());
                return;
            }
        }
        catch (Exception e)
        {
            // 例外をそのまま転送
            callback.onErrorOccurred(e);
            return;
        }
        try
        {
            JSONArray dirsArray = new JSONObject(contentList).getJSONArray("dirs");
            if (dirsArray != null)
            {
                int size = dirsArray.length();
                for (int index = 0; index < size; index++)
                {
                    JSONObject object = dirsArray.getJSONObject(index);
                    String dirName = object.getString("name");
                    JSONArray filesArray = object.getJSONArray("files");
                    int nofFiles = filesArray.length();
                    for (int fileIndex = 0; fileIndex < nofFiles; fileIndex++)
                    {
                        String fileName = filesArray.getString(fileIndex);
                        fileList.add(new CameraFileInfo(dirName, fileName));
                    }
                }
            }
        }
        catch (Exception e)
        {
            callback.onErrorOccurred(e);
            return;
        }
        callback.onCompleted(fileList);
    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info)
    {
        String url = getPhotoUrl + info.getDirectoryPath() + "/" + info.getFilename() + "/info";
        Log.v(TAG, "updateCameraFileInfo() GET URL : " + url);
        try
        {
            String response = SimpleHttpClient.httpGet(url, DEFAULT_TIMEOUT);
            if ((response == null)||(response.length() < 1))
            {
                return;
            }
            JSONObject object = new JSONObject(response);

            // データを突っ込む
            boolean captured = object.getBoolean("captured");
            String av = getJSONString(object, "av");
            String tv = getJSONString(object, "tv");
            String sv = getJSONString(object,"sv");
            String xv = getJSONString(object,"xv");
            int orientation = object.getInt("orientation");
            String aspectRatio = getJSONString(object,"aspectRatio");
            String cameraModel = getJSONString(object,"cameraModel");
            String latLng = getJSONString(object,"latlng");
            String dateTime = object.getString("datetime");
            info.updateValues(dateTime, av, tv, sv, xv, orientation, aspectRatio, cameraModel, latLng, captured);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private String getJSONString(JSONObject object, String key)
    {
        String value = "";
        try
        {
            value = object.getString(key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (value);
    }

    @Override
    public void getContentInfo(@NonNull String path, @NonNull IContentInfoCallback callback)
    {
        String url = getPhotoUrl + path + "/info";
        Log.v(TAG, "getContentInfo() GET URL : " + url);
        try
        {
            String response = SimpleHttpClient.httpGet(url, DEFAULT_TIMEOUT);
            if ((response == null)||(response.length() < 1))
            {
                callback.onErrorOccurred(new NullPointerException());
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContentScreennail(@NonNull String path, @NonNull IDownloadThumbnailImageCallback callback)
    {
        //Log.v(TAG, "downloadContentScreennail() : " + path);
        String suffix = "?size=view";
        String url = getPhotoUrl + path + suffix;
        Log.v(TAG, "downloadContentScreennail() GET URL : " + url);
        try
        {
            Bitmap bmp = SimpleHttpClient.httpGetBitmap(url, DEFAULT_TIMEOUT);
            HashMap<String, Object> map = new HashMap<>();
            map.put("Orientation", 0);
            callback.onCompleted(bmp, map);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContentThumbnail(@NonNull String path, @NonNull IDownloadThumbnailImageCallback callback)
    {
        //Log.v(TAG, "downloadContentThumbnail() : " + path);
        String suffix = "?size=view";
        if (path.contains(".JPG"))
        {
            suffix = "?size=thumb";
        }
        String url = getPhotoUrl + path + suffix;
        Log.v(TAG, "downloadContentThumbnail() GET URL : " + url);
        try
        {
            Bitmap bmp = SimpleHttpClient.httpGetBitmap(url, DEFAULT_TIMEOUT);
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
        Log.v(TAG, "downloadContent() : " + path);
        String suffix = "?size=full";
        if (isSmallSize)
        {
            suffix = "?size=view";
        }
        String url = getPhotoUrl + path + suffix;
        Log.v(TAG, "downloadContent() GET URL : " + url);
        try
        {
            SimpleHttpClient.httpGetBytes(url, DEFAULT_TIMEOUT, new SimpleHttpClient.IReceivedMessageCallback() {
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
     *   撮影時刻は（個別に）取れるが、非常に遅い...
     *
     */
    private Date getCameraContentDate(@NonNull ICameraContent cameraContent)
    {
/*
        String fileInfo;
        try
        {
            String imageInfoUrl = "http://192.168.0.1/v1/photos/" + cameraContent.getContentPath() + "/" + cameraContent.getContentName() + "/info?storage=" + cameraContent.getCardId();
            Log.v(TAG, "getCameraContentDate() : " + imageInfoUrl);
            fileInfo = SimpleHttpClient.httpGet(imageInfoUrl, DEFAULT_TIMEOUT);
            if (fileInfo != null)
            {
                String datetime = new JSONObject(fileInfo).getString("datetime");
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); // "yyyy-MM-dd'T'HH:mm:ssZ"
                dateFormatter.setCalendar(new GregorianCalendar());
                return (dateFormatter.parse(datetime));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
**/
        return (cameraContent.getCapturedDate());
    }

    @Override
    public void getCameraContentList(ICameraContentListCallback callback)
    {
        List<ICameraContent> fileList = new ArrayList<>();
        String imageListurl = "http://192.168.0.1/v1/photos?limit=3000";
        String contentList;
        try
        {
            contentList = SimpleHttpClient.httpGet(imageListurl, DEFAULT_TIMEOUT);
            if (contentList == null)
            {
                // ぬるぽ発行
                callback.onErrorOccurred(new NullPointerException());
                return;
            }
        }
        catch (Exception e)
        {
            // 例外をそのまま転送
            callback.onErrorOccurred(e);
            return;
        }
        try
        {
            String cameraId = statusChecker.getCameraId();
            JSONArray dirsArray = new JSONObject(contentList).getJSONArray("dirs");
            if (dirsArray != null)
            {
                int size = dirsArray.length();
                for (int index = 0; index < size; index++)
                {
                    JSONObject object = dirsArray.getJSONObject(index);
                    String dirName = object.getString("name");
                    JSONArray filesArray = object.getJSONArray("files");
                    int nofFiles = filesArray.length();
                    for (int fileIndex = 0; fileIndex < nofFiles; fileIndex++)
                    {
                        String fileName = filesArray.getString(fileIndex);
                        ICameraContent cameraContent = new CameraContentInfo(cameraId, "sd1", dirName, fileName, new Date());
                        cameraContent.setCapturedDate(getCameraContentDate(cameraContent));
                        fileList.add(cameraContent);
                    }
                }
            }
        }
        catch (Exception e)
        {
            //callback.onErrorOccurred(e);
            //return;
            e.printStackTrace();
            try {
                fileList.clear();
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
            }
        }
        callback.onCompleted(fileList);
    }
}
