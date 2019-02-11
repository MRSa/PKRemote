package net.osdn.gokigen.pkremote.camera.interfaces.playback;


public interface ICameraContentsRecognizer
{
    void getRemoteCameraContentsList(ICameraContentsListCallback callback);

    interface ICameraContentsListCallback
    {
        void contentsListCreated(int nofContents);
    }
}
