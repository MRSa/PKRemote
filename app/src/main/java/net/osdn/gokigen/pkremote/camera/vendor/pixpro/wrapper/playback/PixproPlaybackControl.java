package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.playback;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.IConnectionKeyProvider;

public class PixproPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();

    private final IConnectionKeyProvider keyProvider;

    public PixproPlaybackControl(@NonNull IConnectionKeyProvider keyProvider)
    {
        this.keyProvider = keyProvider;

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
