package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.playback;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraApi;

import org.json.JSONArray;
import org.json.JSONObject;

public class SonyPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private ISonyCameraApi cameraApi = null;

    public SonyPlaybackControl()
    {
        Log.v(TAG, "SonyPlaybackControl()");

    }

    public void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi)
    {
        cameraApi = sonyCameraApi;
    }

    @Override
    public String getRawFileSuffix()
    {
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
        Log.v(TAG, "downloadContentScreennail()");

    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback)
    {
        Log.v(TAG, "downloadContentThumbnail()");

    }

    @Override
    public void downloadContent(String path, boolean isSmallSize, IDownloadContentCallback callback)
    {
        Log.v(TAG, "downloadContent()");

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
                        JSONObject contentObject = resultsArray.getJSONObject(pos);
                        JSONObject contents = contentObject.getJSONObject("content");
                        JSONArray original = contents.getJSONArray("original");
                        String fileNo = contentObject.getString("fileNo");
                        String createdTime = contentObject.getString("createdTime");
                        String contentKind =  contentObject.getString("contentKind");
                        String folderNo =  contentObject.getString("folderNo");
                        String thumbnailUrl = contents.getString("thumbnailUrl");
                        String fileName = original.getJSONObject(0).getString("fileName");

                        Log.v(TAG,  " [" + pos + "] " + "  " + fileName + " " + " " + createdTime + " " + folderNo + " " + thumbnailUrl);
                    }
                    index = index + nofContents;
                    Log.v(TAG, "  COUNT : " + index);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
