package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.playback;

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
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.IConnectionKeyProvider;

import java.util.ArrayList;
import java.util.List;

public class PixproPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private static final int DEFAULT_TIMEOUT = 3000;

    private final String ipAddress;
    private final IConnectionKeyProvider keyProvider;
    private final int timeoutValue;

    private List<ICameraContent> cameraContentList;

    public PixproPlaybackControl(@NonNull String ipAddress, int timeoutMs, @NonNull IConnectionKeyProvider keyProvider)
    {
        this.ipAddress = ipAddress;
        this.keyProvider = keyProvider;
        this.timeoutValue  = Math.max(timeoutMs, DEFAULT_TIMEOUT);
        cameraContentList = new ArrayList<>();

    }


    @Override
    public String getRawFileSuffix()
    {
        return (null);
    }

    @Override
    public void downloadContentList(IDownloadContentListCallback callback)
    {

    }

    @Override
    public void getContentInfo(String path, String name, IContentInfoCallback callback)
    {

    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info)
    {

    }

    @Override
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback)
    {

    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback)
    {

    }

    @Override
    public void downloadContent(String path, boolean isSmallSize, IDownloadContentCallback callback)
    {

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
                cameraContentList.clear();
                return;
            }
            // 応答を受信した場合...
            Log.v(TAG, " RECEIVED CONTENT REPLY : " + receivedMessage.length());
            parseContentList(receivedMessage);
            callback.onCompleted(cameraContentList);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            // 例外時にはぬるぽを発行する
            callback.onErrorOccurred(new NullPointerException());
        }
    }

    private void parseContentList(@NonNull String receivedMessage)
    {
        // 受信したボディを解析して、画像一覧を cameraContentList に入れる


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
        String connectionString = keyProvider.getUserString() + "&" + keyProvider.getPasswordString();
        Log.v(TAG, " connectionString : " + connectionString);
        return (connectionString);
    }

}
