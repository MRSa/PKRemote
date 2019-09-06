package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.playback;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraApi;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SonyPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private final Activity activity;
    private ISonyCameraApi cameraApi = null;
    private HashMap<String, SonyImageContentInfo> contentList;
    private int timeoutMs = 50000;

    public SonyPlaybackControl(@NonNull Activity activity)
    {
        Log.v(TAG, "SonyPlaybackControl()");
        this.activity = activity;
        contentList = new HashMap<>();

    }

    public void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi) {
        cameraApi = sonyCameraApi;
    }

    @Override
    public String getRawFileSuffix() {
        return "ARW";
    }

    @Override
    public void downloadContentList(IDownloadContentListCallback callback)
    {
        Log.v(TAG, "downloadContentList()");

    }

    @Override
    public void getContentInfo(String path, String name, IContentInfoCallback callback)
    {
        Log.v(TAG, "getContentInfo()");
    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info)
    {
        Log.v(TAG, "updateCameraFileInfo()");
    }

    @Override
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback)
    {
        Log.v(TAG, "downloadContentScreennail()" + path);
        try
        {
            SonyImageContentInfo content = contentList.get(path.substring(path.indexOf('/') + 1));
            if (content == null)
            {
                Log.v(TAG, " CONTENT IS NULL... : " + path);
                return;
            }
            try
            {
                String url = content.getSmallUrl();   // Screennail は VGAサイズ
                if (url.length() < 1)
                {
                    url = content.getThumbnailUrl();  // VGAサイズが取れなかった場合はサムネイルサイズ
                }
                if (url.length() > 1)
                {
                    Bitmap bmp = SimpleHttpClient.httpGetBitmap(url, timeoutMs);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("Orientation", 0);
                    callback.onCompleted(bmp, map);
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                callback.onErrorOccurred(new NullPointerException());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback)
    {
        Log.v(TAG, "downloadContentThumbnail() : " + path);
        try
        {
            SonyImageContentInfo content = contentList.get(path.substring(path.indexOf('/') + 1));
            if (content == null)
            {
                Log.v(TAG, " CONTENT IS NULL... : " + path);
                return;
            }
            try
            {
                String url = content.getThumbnailUrl();
                if (url.length() > 1)
                {
                    Bitmap bmp = SimpleHttpClient.httpGetBitmap(url, timeoutMs);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("Orientation", 0);
                    callback.onCompleted(bmp, map);
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                callback.onErrorOccurred(new NullPointerException());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContent(String path, boolean isSmallSize, final IDownloadContentCallback callback)
    {
        Log.v(TAG, "downloadContent() : " + path);
        try
        {
            SonyImageContentInfo content = contentList.get(path.substring(path.indexOf('/') + 1));
            if (content == null)
            {
                Log.v(TAG, " CONTENT IS NULL... : " + path);
                return;
            }
            try
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                boolean isVgaSize = preferences.getBoolean(IPreferencePropertyAccessor.GET_SMALL_PICTURE_AS_VGA, false);
                String url = (isSmallSize) ? ((isVgaSize) ? content.getSmallUrl() : content.getLargeUrl()) : content.getOriginalUrl();
                if (url.length() < 1)
                {
                    url = content.getOriginalUrl();
                    if (url.length() < 1)
                    {
                        //  全然だめなら、サムネイルサイズ...
                        url = content.getThumbnailUrl();
                    }
                }
                Log.v(TAG, "downloadContent()  PATH : " + path + "  [SMALL:" + isSmallSize + "][VGA:" + isVgaSize + "]" + " GET URL : " + url);


                SimpleHttpClient.httpGetBytes(url, timeoutMs, new SimpleHttpClient.IReceivedMessageCallback() {
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
                callback.onErrorOccurred(new NullPointerException());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }




    }

    @Override
    public void getCameraContentList(ICameraContentListCallback callback)
    {
        Log.v(TAG, "getCameraContentList()");
        try
        {
            if (cameraApi == null)
            {
                Log.v(TAG, "CAMERA API is NULL.");
                return;
            }
            JSONObject storageInformationObj = cameraApi.getStorageInformation();
            JSONObject schemeListObj = cameraApi.getSchemeList();
            //JSONArray schemeArray = schemeListObj.getJSONArray("result");
            JSONObject sourceObj = cameraApi.getSourceList("storage");
            //JSONArray sourceArray = sourceObj.getJSONArray("result");
            JSONObject countObject = cameraApi.getContentCountFlatAll("storage:memoryCard1");
            JSONArray resultArray = countObject.getJSONArray("result");
            int objectCount = resultArray.getJSONObject(0).getInt("count");
            Log.v(TAG, "  OBJECT COUNT  : " + objectCount);
            contentList.clear();

            int index = 0;
            // データを解析してリストを作る
            while ((index >= 0) && (index < objectCount))
            {
                int remainCount = objectCount - index;
                JSONObject paramsObj = new JSONObject();
                paramsObj.put("uri", "storage:memoryCard1");
                paramsObj.put("stIdx", index);
                paramsObj.put("cnt", (remainCount > 100 ? 100 : remainCount));
                paramsObj.put("view", "flat");
                paramsObj.put("sort", "descending");
                try
                {
                    JSONObject responseObject = cameraApi.getContentList(new JSONArray().put(paramsObj));
                    JSONArray resultsArray = responseObject.getJSONArray("result").getJSONArray(0);
                    int nofContents = resultsArray.length();
                    for (int pos = 0; pos < nofContents; pos++)
                    {
                        //  ひろったデータを全部入れていく
                        SonyImageContentInfo contentInfo = new SonyImageContentInfo(resultsArray.getJSONObject(pos));
                        String contentName = contentInfo.getContentName();
                        Date createdTime = contentInfo.getCapturedDate();
                        String folderNo = contentInfo.getContentPath();
                        if (contentName.length() > 0)
                        {
                            contentList.put(contentName, contentInfo);
                        }
                        //Log.v(TAG, " [" + pos + "] " + "  " + contentName + " " + " " + createdTime + " " + folderNo);
                    }
                    index = index + nofContents;
                    //Log.v(TAG, "  COUNT : " + index);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    break;
                }
            }
            if (callback != null)
            {
                // コレクションを詰めなおして応答する
                callback.onCompleted(new ArrayList<ICameraContent>(contentList.values()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



}
