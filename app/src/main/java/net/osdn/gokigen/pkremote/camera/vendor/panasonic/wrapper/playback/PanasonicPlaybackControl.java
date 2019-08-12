package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.playback;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PanasonicPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private IPanasonicCamera panasonicCamera = null;
    private int timeoutMs = 50000;
    private String getObjectLists;
    private List<ICameraContent> contentList;

    public PanasonicPlaybackControl()
    {
        contentList = new ArrayList<>();
    }

    public void setCamera(IPanasonicCamera panasonicCamera, int timeoutMs)
    {
        Log.v(TAG, "setCamera() " + panasonicCamera.getFriendlyName());
        this.panasonicCamera = panasonicCamera;
        this.timeoutMs = timeoutMs;
    }

    public void preprocessPlaymode()
    {
        // PLAYBACKモードに切り替わった直後に実行する処理をここに書く。
        Log.v(TAG, "  preprocessPlaymode() : " + panasonicCamera.getObjUrl());

        String url = panasonicCamera.getObjUrl() + "Server0/CDS_control";
        String postData = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>" +
                "<u:Browse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:1\" xmlns:pana=\"urn:schemas-panasonic-com:pana\">" +
                "<ObjectID>0</ObjectID><BrowseFlag>BrowseDirectChildren</BrowseFlag><Filter>*</Filter><StartingIndex>0</StartingIndex><RequestedCount>1500</RequestedCount><SortCriteria></SortCriteria>" +
                "<pana:X_FromCP>LumixLink2.0</pana:X_FromCP></u:Browse></s:Body></s:Envelope>";

        String reply = SimpleHttpClient.httpPostWithHeader(url, postData, "SOAPACTION", "urn:schemas-upnp-org:service:ContentDirectory:1#Browse", "text/xml; charset=\"utf-8\"", timeoutMs);
        getObjectLists = reply;
        String matches = reply.substring(reply.indexOf("<TotalMatches>") + 14, reply.indexOf("</TotalMatches>"));
        String returned = reply.substring(reply.indexOf("<NumberReturned>") + 16, reply.indexOf("</NumberReturned>"));;
        Log.v(TAG, "REPLY DATA : (" + matches + ") [" + returned + "] " + " " + reply.length() + "bytes");
    }

    @Override
    public String getRawFileSuffix()
    {
        Log.v(TAG, "getRawFileSuffix()");
        return ("RW2");
    }

    @Override
    public void downloadContentList(IDownloadContentListCallback callback)
    {
        Log.v(TAG, "downloadContentList()");

    }

    @Override
    public void getContentInfo(String path, String name, IContentInfoCallback callback)
    {
        Log.v(TAG, "getContentInfo() : " + path + " / " + name);


    }

    @Override
    public void updateCameraFileInfo(@NonNull ICameraFileInfo info)
    {
        Log.v(TAG, "updateCameraFileInfo() : " + info.getFilename());


    }

    @Override
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback)
    {
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        String requestUrl =  panasonicCamera.getPictureUrl() + "DL" + path.substring(2);
        Log.v(TAG, " downloadContentScreennail() : " + requestUrl + "  ");
        try
        {
            Bitmap bmp = SimpleHttpClient.httpGetBitmap(requestUrl, timeoutMs);
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
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback)
    {
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        String requestUrl =  panasonicCamera.getPictureUrl() + "DT" + path.substring(2);
        Log.v(TAG, " downloadContentThumbnail() : " + path + "  [" +  requestUrl + "]");
        try
        {
            Bitmap bmp = SimpleHttpClient.httpGetBitmap(requestUrl, timeoutMs);
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
        Log.v(TAG, "downloadContent() : " + path + "  [" + isSmallSize + "]");


    }

    @Override
    public void getCameraContentList(ICameraContentListCallback callback)
    {
        Log.v(TAG, "  getCameraContentList()");
        contentList.clear();
        try
        {
            String checkUrl = panasonicCamera.getPictureUrl();
            int maxIndex = getObjectLists.length() - checkUrl.length();
            int index = 0;

            // データを解析してリストを作る
            while ((index >= 0) && (index < maxIndex))
            {
                index = getObjectLists.indexOf(checkUrl, index);
                if (index > 0)
                {
                    int lastIndex = getObjectLists.indexOf("&", index);
                    String picUrl = getObjectLists.substring(index + checkUrl.length(), lastIndex);
                    // Log.v(TAG, " pic : " + picUrl);
                    if (picUrl.startsWith("DO"))
                    {
                        // DO(オリジナル), DL(スクリーンネイル?), DT(サムネイル?)
                        PanasonicImageContentInfo contentInfo = new PanasonicImageContentInfo(picUrl);
                        contentList.add(contentInfo);
                    }
                    index = lastIndex;
                }
            }

            if (callback != null)
            {
                callback.onCompleted(contentList);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            if (callback != null)
            {
                callback.onErrorOccurred(e);
            }
        }
    }
}
