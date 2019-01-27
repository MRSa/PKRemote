package net.osdn.gokigen.pkremote.camera.interfaces.playback;

import java.util.List;

public interface IDownloadContentListCallback
{
    void onCompleted(List<ICameraFileInfo> contentList);
    void onErrorOccurred(Exception  e);
}
