package net.osdn.gokigen.pkremote.camera.interfaces.playback;


import java.util.List;

public interface ICameraContentsRecognizer
{
    void getRemoteCameraContentsList(boolean isReload, ICameraContentsListCallback callback);
    List<ICameraContent> getContentsList();

    interface ICameraContentsListCallback
    {
        void contentsListCreated(int nofContents);
    }
}
