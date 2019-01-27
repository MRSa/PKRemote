package net.osdn.gokigen.pkremote.camera.interfaces.playback;

public interface IDownloadContentCallback
{
    void onCompleted();
    void onErrorOccurred(Exception  e);
    void onProgress(byte[] data, int length, IProgressEvent e);
}
