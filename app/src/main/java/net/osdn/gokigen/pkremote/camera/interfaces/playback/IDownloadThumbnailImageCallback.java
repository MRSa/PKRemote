package net.osdn.gokigen.pkremote.camera.interfaces.playback;

import android.graphics.Bitmap;

import java.util.Map;

public interface IDownloadThumbnailImageCallback
{
    void onCompleted(Bitmap bitmap, Map<String, Object> metadata);
    void onErrorOccurred(Exception  e);
}
