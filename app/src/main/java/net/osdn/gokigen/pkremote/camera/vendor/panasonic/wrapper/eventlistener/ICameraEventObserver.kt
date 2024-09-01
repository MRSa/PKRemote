package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;

public interface ICameraEventObserver
{
    void activate();
    boolean start();
    void stop();
    void release();

    void setEventListener(@NonNull ICameraChangeListener listener);
    void clearEventListener();

    ICameraStatusHolder getCameraStatusHolder();
}
