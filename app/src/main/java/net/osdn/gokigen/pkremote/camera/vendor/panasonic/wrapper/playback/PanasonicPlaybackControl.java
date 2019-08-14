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
import net.osdn.gokigen.pkremote.camera.playback.ProgressEvent;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

public class PanasonicPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private static final int COMMAND_POLL_QUEUE_MS = 50;
    private IPanasonicCamera panasonicCamera = null;
    private int timeoutMs = 50000;
    private int sequenceNumber = 0;
    private boolean isStarted = false;
    private String getObjectLists;
    private List<ICameraContent> contentList;
    private Queue<DownloadScreennailRequest> commandQueue;


    public PanasonicPlaybackControl()
    {
        contentList = new ArrayList<>();
    }

    public void setCamera(IPanasonicCamera panasonicCamera, int timeoutMs)
    {
        Log.v(TAG, "setCamera() " + panasonicCamera.getFriendlyName());
        this.panasonicCamera = panasonicCamera;
        this.timeoutMs = timeoutMs;
        this.commandQueue = new ArrayDeque<>();
        commandQueue.clear();
    }

    private void getContentList()
    {
        if (panasonicCamera == null)
        {
            // URLが特定できていないため、送信できないので先に進める
            return;
        }

        // PLAYモードに切り替える
        String requestUrl = this.panasonicCamera.getCmdUrl() + "cam.cgi?mode=camcmd&value=playmode";
        String reqPlay = SimpleHttpClient.httpGet(requestUrl, this.timeoutMs);
        if (!reqPlay.contains("ok"))
        {
            Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE PLAYMODE.");
        }

        Log.v(TAG, "  ===== getContentList() " + sequenceNumber + " =====");
        sequenceNumber++;
        String url = panasonicCamera.getObjUrl() + "Server0/CDS_control";
        String postData = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>" +
                "<u:Browse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:" + sequenceNumber + "\" xmlns:pana=\"urn:schemas-panasonic-com:pana\">" +
                "<ObjectID>0</ObjectID><BrowseFlag>BrowseDirectChildren</BrowseFlag><Filter>*</Filter><StartingIndex>0</StartingIndex><RequestedCount>3500</RequestedCount><SortCriteria></SortCriteria>" +
                "<pana:X_FromCP>LumixLink2.0</pana:X_FromCP></u:Browse></s:Body></s:Envelope>";

        String reply = SimpleHttpClient.httpPostWithHeader(url, postData, "SOAPACTION", "urn:schemas-upnp-org:service:ContentDirectory:" + sequenceNumber + "#Browse", "text/xml; charset=\"utf-8\"", timeoutMs);
        if (reply.length() < 10)
        {
            Log.v(TAG, postData);
            Log.v(TAG, "ContentDirectory is FAILURE. [" + sequenceNumber + "]");
            return;
        }
        getObjectLists = reply;
        String matches = reply.substring(reply.indexOf("<TotalMatches>") + 14, reply.indexOf("</TotalMatches>"));
        String returned = reply.substring(reply.indexOf("<NumberReturned>") + 16, reply.indexOf("</NumberReturned>"));
        Log.v(TAG, "REPLY DATA : (" + matches + ") [" + returned + "] " + " " + reply.length() + "bytes");
    }

    public void preprocessPlaymode()
    {
        // PLAYBACKモードに切り替わった直後に実行する処理をここに書く。
        Log.v(TAG, "  preprocessPlaymode() : " + panasonicCamera.getObjUrl());

        // 画像情報を取得
        sequenceNumber = 0;
        getContentList();

        // スクリーンネイルを１こづつ取得するように変更
        getScreenNailService();
    }

    @Override
    public String getRawFileSuffix()
    {
        Log.v(TAG, " getRawFileSuffix()");
        return ("RW2");
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
        //　画像の情報を取得する

    }

    @Override
    public void updateCameraFileInfo(@NonNull ICameraFileInfo info)
    {
        Log.v(TAG, " updateCameraFileInfo() : " + info.getFilename());


    }

    @Override
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback)
    {
        commandQueue.add(new DownloadScreennailRequest(path, callback));
    }

    /**
     *   スクリーンネイルを取得するロジック
     *
     */
    private void getScreenNailService()
    {
        if (isStarted)
        {
            // すでにスタートしている場合は、スレッドを走らせない
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true)
                {
                    try
                    {
                        DownloadScreennailRequest request = commandQueue.poll();
                        if (request != null)
                        {
                            downloadContentScreennailImpl(request.getPath(), request.getCallback());
                        }
                        Thread.sleep(COMMAND_POLL_QUEUE_MS);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        try
        {
            isStarted = true;
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void downloadContentScreennailImpl(String path, IDownloadThumbnailImageCallback callback)
    {
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        String requestUrl =  panasonicCamera.getPictureUrl() + "DL" + path.substring(2, path.lastIndexOf(".")) + ".JPG";
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
        String requestUrl =  panasonicCamera.getPictureUrl() + "DT" + path.substring(2, path.lastIndexOf(".")) + ".JPG";
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
    public void downloadContent(String path, boolean isSmallSize, final IDownloadContentCallback callback)
    {
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        String url =  panasonicCamera.getPictureUrl() + path;
        if (isSmallSize)
        {
            url =  panasonicCamera.getPictureUrl() + "DL" + path.substring(2, path.lastIndexOf(".")) + ".JPG";
        }
        Log.v(TAG, "downloadContent()  PATH : " + path + " GET URL : " + url + "  [" + isSmallSize + "]");

        try
        {
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
        Log.v(TAG, "  getCameraContentList()");

        // 画像情報を取得
        getContentList();

        contentList.clear();
        try
        {
            if (getObjectLists == null)
            {
                //何もしないで終了する
                return;
            }
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
                    if (picUrl.startsWith("DO"))
                    {
                        // DO(オリジナル), DL(スクリーンネイル?), DT(サムネイル?)
                        //Log.v(TAG, " pic : " + picUrl);
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

    /**
     *   スクリーンネイルの取得キューで使用するクラス
     */
    private class DownloadScreennailRequest
    {
        private final String path;
        private final IDownloadThumbnailImageCallback callback;
        DownloadScreennailRequest(String path, IDownloadThumbnailImageCallback callback)
        {
            this.path = path;
            this.callback = callback;
        }

        String getPath()
        {
            return (path);
        }
        IDownloadThumbnailImageCallback getCallback()
        {
            return (callback);
        }
    }
}
