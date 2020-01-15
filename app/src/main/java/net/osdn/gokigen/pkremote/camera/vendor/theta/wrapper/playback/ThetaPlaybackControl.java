package net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.playback;

import android.app.Activity;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;

public class ThetaPlaybackControl implements IPlaybackControl
{
    public ThetaPlaybackControl(@NonNull Activity activity, int timeoutMs)
    {

    }


    @Override
    public String getRawFileSuffix() {
        return null;
    }

    @Override
    public void downloadContentList(IDownloadContentListCallback callback) {

    }

    @Override
    public void getContentInfo(String path, String name, IContentInfoCallback callback) {

    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info) {

    }

    @Override
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback) {

    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback) {

    }

    @Override
    public void downloadContent(String path, boolean isSmallSize, IDownloadContentCallback callback) {

    }

    @Override
    public void getCameraContentList(ICameraContentListCallback callback) {

    }

    @Override
    public void showPictureStarted() {

    }

    @Override
    public void showPictureFinished() {

    }
}
