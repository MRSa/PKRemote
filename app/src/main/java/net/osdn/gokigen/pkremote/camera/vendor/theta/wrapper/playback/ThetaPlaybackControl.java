package net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.playback;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.playback.ProgressEvent;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThetaPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_MAX_COUNT = 300;
    private final int timeoutValue;
    private final int maxCount;
    private final boolean useThetaV21;

    private List<ICameraContent> cameraContentList;

    public ThetaPlaybackControl(@NonNull Activity activity, int timeoutMs, int maxCount)
    {
        this.timeoutValue  = (timeoutMs < DEFAULT_TIMEOUT) ? DEFAULT_TIMEOUT : timeoutMs;
        this.maxCount  = (maxCount < DEFAULT_MAX_COUNT) ? DEFAULT_MAX_COUNT : maxCount;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        useThetaV21 = preferences.getBoolean(IPreferencePropertyAccessor.USE_OSC_THETA_V21, false);
        cameraContentList = new ArrayList<>();

    }

    @Override
    public String getRawFileSuffix() {
        return ".DNG";
    }

    @Override
    public void downloadContentList(IDownloadContentListCallback callback)
    {
        Log.v(TAG, " downloadContentList()");
    }

    @Override
    public void getContentInfo(String path, String name, IContentInfoCallback callback)
    {
        Log.v(TAG, " getContentInfo() : " + path + " / " + name);

    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info)
    {
        Log.v(TAG, " updateCameraFileInfo() : " + info.getFilename());
    }

    @Override
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback)
    {
        downloadContentThumbnail(path, callback);
    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback)
    {
        if (useThetaV21) {
            downloadContentThumbnailV21(path, callback);
        } else {
            downloadContentThumbnailV2(path, callback);
        }
    }

    private void downloadContentThumbnailV2(String path, IDownloadThumbnailImageCallback callback)
    {
        //  POST /osc/commands/execute
        //  {"name":"camera.getImage","parameters":{"_type":"thumb","fileUri":"100RICOH/R0010093.JPG"}}
        Log.v(TAG, "downloadContentThumbnail() : " + path);
        try
        {
            String url = "http://192.168.1.1/osc/commands/execute";
            String postData = "{\"name\":\"camera.getImage\",\"parameters\":{\"_type\":\"thumb\",\"fileUri\":\"" + path + "\"}}";
            //Log.v(TAG, " postData : " + postData);

            Bitmap bmp = SimpleHttpClient.httpPostBitmap(url, postData, timeoutValue);
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

    private void downloadContentThumbnailV21(String path, IDownloadThumbnailImageCallback callback)
    {
        //  fileContent の URLをそのまま使用する
        Log.v(TAG, "  downloadContentThumbnailV21() : " + path);
        String paramData = null;
        try
        {
            //String url = "http://" + path;
            int index = 0;
            for (ICameraContent content : cameraContentList)
            {
                String targetPath = content.getContentPath() + "/" + content.getContentName();
                if (targetPath.equals(path))
                {
                    //Log.v(TAG, " MATCHED : (" + index + ") " + path);
                    paramData = "{\"name\":\"camera.listFiles\",\"parameters\":{\"fileType\":\"all\",\"startPosition\": " + index + ",\"entryCount\":" + 1 + ",\"maxThumbSize\":640,\"_detail\":true, \"_sort\":\"newest\"}}";
                    break;
                }
                index++;
            }
            //Log.v(TAG, " downloadContentThumbnailV21 : " + paramData);
            Bitmap bmp = null; // SimpleHttpClient.httpGetBitmap(url, null, timeoutValue);
            if (paramData != null)
            {
                try
                {
                    //Log.v(TAG, " exec getFileList");
                    String imageListurl = "http://192.168.1.1/osc/commands/execute";
                    String contentList = SimpleHttpClient.httpPost(imageListurl, paramData, timeoutValue);
                    if (contentList != null)
                    {
                        JSONObject resultsObject = new JSONObject(contentList).getJSONObject("results");
                        JSONArray entriesArray = resultsObject.getJSONArray("entries");
                        int size = entriesArray.length();
                        if (size > 0)
                        {
                            JSONObject object = entriesArray.getJSONObject(0);
                            //String fileName = object.getString("name");
                            //String fileDateTime = object.getString("dateTimeZone"); // detail : true
                            byte[] thumb = Base64.decode( object.getString("thumbnail"), Base64.DEFAULT);     // detail : true (Base64)
                            bmp = BitmapFactory.decodeByteArray(thumb, 0, thumb.length);
                            //Log.v(TAG, " ----- camera.listFiles : " + fileName + " " + fileDateTime + " [" + thumb.length + "] (" + size + ")");
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
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
    public void downloadContent(String path, boolean isSmallSize, IDownloadContentCallback callback)
    {
        try
        {
            if (useThetaV21) {
                downloadContentImplV21(path, isSmallSize, callback);
            } else {
                downloadContentImpl(path, isSmallSize, callback);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void downloadContentImplV21(String path, boolean isSmallSize, final IDownloadContentCallback callback)
    {
        //Log.v(TAG, "downloadContentV21() : " + path + " (small :" + isSmallSize + ")");
        final String urlToGet = "http://" + path;
        try
        {
            SimpleHttpClient.httpGetBytes(urlToGet, null, timeoutValue, new SimpleHttpClient.IReceivedMessageCallback() {
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

    private void downloadContentImpl(String path, boolean isSmallSize, final IDownloadContentCallback callback)
    {
        Log.v(TAG, "downloadContent() : " + path + " (small :" + isSmallSize + ")");
        String urlToGet = "http://192.168.1.1/osc/commands/execute";
        String postData = "{\"name\":\"camera.getImage\",\"parameters\":{\"_type\":\"full\",\"fileUri\":\"" + path + "\"}}";
        try
        {
            SimpleHttpClient.httpPostBytes(urlToGet, postData, null, timeoutValue, new SimpleHttpClient.IReceivedMessageCallback() {
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

    @Override
    public void getCameraContentList(ICameraContentListCallback callback)
    {
        try
        {
            if (useThetaV21) {
                getCameraContentListImplV21(callback);
            } else {
                getCameraContentListImpl(callback);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

    private void getCameraContentListImpl(ICameraContentListCallback callback)
    {
        String imageListurl = "http://192.168.1.1/osc/commands/execute";
        String paramStr = "{\"name\":\"camera._listAll\",\"parameters\":{\"detail\":false,\"entryCount\":" + maxCount + ",\"sort\":\"newest\"}}";
        String contentList;
        try
        {
            contentList = SimpleHttpClient.httpPost(imageListurl, paramStr, timeoutValue);
            if (contentList == null)
            {
                // ぬるぽ発行
                callback.onErrorOccurred(new NullPointerException());
                cameraContentList.clear();
                return;
            }
        }
        catch (Exception e)
        {
            // 例外をそのまま転送
            callback.onErrorOccurred(e);
            //cameraContentList.clear();
            return;
        }
        try
        {
            Log.v(TAG, "PHOTO LIST RECV: [" + contentList.length() + "] ");
            JSONObject resultsObject = new JSONObject(contentList).getJSONObject("results");
            JSONArray entriesArray = resultsObject.getJSONArray("entries");
            int size = entriesArray.length();
            cameraContentList.clear();
            for (int index = 0; index < size; index++)
            {
                JSONObject object = entriesArray.getJSONObject(index);
                String fileName = object.getString("name");
                String fileUri = object.getString("uri");
                String fileSize = object.getString("size");
                String fileDateTime = object.getString("dateTime");

                cameraContentList.add(new ThetaCameraContent(fileName, fileUri, null, fileSize, fileDateTime));
                //Log.v(TAG, " [" + (index + 1) + "] " + fileName + " " + fileUri + " " + fileSize + " " + fileDateTime + " ");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        callback.onCompleted(cameraContentList);
    }

    private void getCameraContentListImplV21(ICameraContentListCallback callback)
    {
        String imageListurl = "http://192.168.1.1/osc/commands/execute";
        String contentList;
        try
        {
            String paramStr = "{\"name\":\"camera.listFiles\",\"parameters\":{\"fileType\":\"all\",\"entryCount\":" +  maxCount + ",\"maxThumbSize\":640,\"_detail\":false, \"_sort\":\"newest\"}}";
            //Log.v(TAG, " paramStr : " + paramStr);
            contentList = SimpleHttpClient.httpPost(imageListurl, paramStr, timeoutValue);
            if (contentList == null)
            {
                // ぬるぽ発行
                callback.onErrorOccurred(new NullPointerException());
                cameraContentList.clear();
                return;
            }
        }
        catch (Exception e)
        {
            // 例外をそのまま転送
            callback.onErrorOccurred(e);
            //cameraContentList.clear();
            return;
        }
        try
        {
            Log.v(TAG, "PHOTO LIST RECV: [" + contentList.length() + "] ");
            JSONObject resultsObject = new JSONObject(contentList).getJSONObject("results");
            JSONArray entriesArray = resultsObject.getJSONArray("entries");
            int size = entriesArray.length();
            cameraContentList.clear();
            for (int index = 0; index < size; index++)
            {
                JSONObject object = entriesArray.getJSONObject(index);
                String fileName = object.getString("name");
                String fileUri = object.getString("fileUrl");
                String fileSize = object.getString("size");
                String fileDateTime = object.getString("dateTime");    // detail : false

                cameraContentList.add(new ThetaCameraContent(fileName, null, fileUri, fileSize, fileDateTime));
                //Log.v(TAG, " [" + (index + 1) + "] " + fileName + " " + fileUri + " " + fileSize + " " + fileDateTime);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            cameraContentList.clear();
        }
        callback.onCompleted(cameraContentList);
    }
}
