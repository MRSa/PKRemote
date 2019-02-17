package net.osdn.gokigen.pkremote.camera.interfaces.playback;


import android.widget.ArrayAdapter;

import java.util.List;

public interface ICameraContentsRecognizer
{
    void getRemoteCameraContentsList(boolean isReload, ICameraContentsListCallback callback);
    List<ICameraContent> getContentsList();
    List<String> getDateList();
    List<String> getPathList();

    interface ICameraContentsListCallback
    {
        void contentsListCreated(int nofContents);
    }
}
