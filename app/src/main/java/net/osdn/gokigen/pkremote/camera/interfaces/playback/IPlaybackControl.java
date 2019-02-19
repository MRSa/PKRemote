package net.osdn.gokigen.pkremote.camera.interfaces.playback;

/**
 *   画像再生・取得用インタフェース
 *
 */
public interface IPlaybackControl
{
    String getRawFileSuffix();
    void downloadContentList(IDownloadContentListCallback callback);
    void getContentInfo(String  path, String name, IContentInfoCallback callback);
    void updateCameraFileInfo(ICameraFileInfo info);

    void downloadContentScreennail(String  path, IDownloadThumbnailImageCallback callback);
    void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback);
    void downloadContent(String  path, boolean isSmallSize, IDownloadContentCallback callback);

    void getCameraContentList(ICameraContentListCallback callback);
}
