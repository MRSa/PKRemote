package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;

public interface ISonyCameraHolder
{
    void detectedCamera(@NonNull ISonyCamera camera);
    void prepare();
    void startRecMode();
    void startEventWatch(@Nullable ICameraChangeListener listener);
}
