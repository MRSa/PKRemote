package net.osdn.gokigen.pkremote.camera.interfaces.playback;

import java.util.List;

public interface ICameraContentListCallback
{
    void onCompleted(List<ICameraContent> contentList);
    void onErrorOccurred(Exception  e);
}
