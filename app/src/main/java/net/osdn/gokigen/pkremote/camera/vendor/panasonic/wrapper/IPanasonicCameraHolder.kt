package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;

public interface IPanasonicCameraHolder
{
    void detectedCamera(IPanasonicCamera camera);
    void prepare();
    void startRecMode();
    void startPlayMode();
    void startEventWatch(@Nullable ICameraChangeListener listener);
}
