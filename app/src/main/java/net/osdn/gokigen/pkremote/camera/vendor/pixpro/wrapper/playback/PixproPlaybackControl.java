package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.playback;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;

public class PixproPlaybackControl implements IPlaybackControl
{
    public PixproPlaybackControl()
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
