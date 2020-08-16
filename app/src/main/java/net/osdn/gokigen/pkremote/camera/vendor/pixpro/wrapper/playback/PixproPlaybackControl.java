package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.playback;

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
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.IConnectionKeyProvider;

import java.util.HashMap;
import java.util.List;

public class PixproPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private static final int DEFAULT_TIMEOUT = 3000;

    private final String ipAddress;
    private final IConnectionKeyProvider keyProvider;
    private final int timeoutValue;

    private PixproContentListParser contentListParser;

    public PixproPlaybackControl(@NonNull String ipAddress, int timeoutMs, @NonNull IConnectionKeyProvider keyProvider)
    {
        this.ipAddress = ipAddress;
        this.keyProvider = keyProvider;
        this.timeoutValue  = Math.max(timeoutMs, DEFAULT_TIMEOUT);
        contentListParser = new PixproContentListParser();
    }


    @Override
    public String getRawFileSuffix()
    {
        return (null);
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
/*
        try
        {
            int index = path.indexOf(".");
            String urlToGet = "http://" + ipAddress + path.substring(0, index) + ".scn?" + getConnectionString();
            Log.v(TAG, "downloadContentThumbnail() : " + urlToGet);

            Bitmap bmp = SimpleHttpClient.httpGetBitmap(urlToGet, null, timeoutValue);
            HashMap<String, Object> map = new HashMap<>();
            map.put("Orientation", 0);
            callback.onCompleted(bmp, map);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            callback.onErrorOccurred(new NullPointerException());
        }
*/
    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback)
    {
        try
        {
            int index = path.indexOf(".");
            String urlToGet = "http://" + ipAddress + path.substring(0, index) + ".thm?" + getConnectionString();
            Log.v(TAG, "downloadContentThumbnail() : " + urlToGet);

            Bitmap bmp = SimpleHttpClient.httpGetBitmap(urlToGet, null, timeoutValue);
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
        try
        {
            String urlToGet = "http://" + ipAddress + path + "?" + getConnectionString();
            if (isSmallSize)
            {
                int index = path.indexOf(".");
                urlToGet = "http://" + ipAddress + path.substring(0, index) + ".scn?" + getConnectionString();
                urlToGet = urlToGet.toLowerCase();
                urlToGet = urlToGet.replace("dcim", "scn");
            }
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
            callback.onErrorOccurred(new NullPointerException());
        }
    }

    @Override
    public void getCameraContentList(ICameraContentListCallback callback)
    {
        try
        {
            String imageListurl = "http://" + ipAddress + "/?custom=1&" + getConnectionString();
            String receivedMessage = SimpleHttpClient.httpGet(imageListurl, timeoutValue);
            if (receivedMessage == null)
            {
                // ぬるぽ発行
                callback.onErrorOccurred(new NullPointerException());
                return;
            }
            // 応答を受信した場合...受信データを parseして応答する。
            Log.v(TAG, " RECEIVED CONTENT REPLY : " + receivedMessage.length());
            List<ICameraContent> cameraContentList = contentListParser.parseContentList(receivedMessage);
            callback.onCompleted(cameraContentList);
            System.gc();
        }
        catch (Exception e)
        {
            e.printStackTrace();

            // 例外時にはぬるぽを発行する
            callback.onErrorOccurred(new NullPointerException());
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

    private String getConnectionString()
    {
        return (keyProvider.getUserString() + "&" + keyProvider.getPasswordString());
        // Log.v(TAG, " connectionString : " + connectionString);
        //return (connectionString);
    }

}
