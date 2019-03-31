package net.osdn.gokigen.pkremote.playback;

import android.net.Uri;

public interface IContentDownloadNotify
{
    void downloadedImage(String contentInfo, Uri content);
}
