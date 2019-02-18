package net.osdn.gokigen.pkremote.camera.interfaces.playback;

import java.util.List;

public interface ICameraContentsRecognizer
{
    void getRemoteCameraContentsList(boolean isReload, ICameraContentsListCallback callback);
    List<ICameraContent> getContentsList();
    List<ICameraContent> getContentsListAtDate(String date);
    List<ICameraContent> getContentsListAtPath(String path);

    List<String> getDateList();
    List<String> getPathList();

    interface ICameraContentsListCallback
    {
        void contentsListCreated(int nofContents);
    }
}
