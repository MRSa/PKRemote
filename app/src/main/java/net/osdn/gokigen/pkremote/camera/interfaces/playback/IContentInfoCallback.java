package net.osdn.gokigen.pkremote.camera.interfaces.playback;

public interface IContentInfoCallback
{
    void onCompleted(ICameraFileInfo fileInfo);
    void onErrorOccurred(Exception  e);
}
