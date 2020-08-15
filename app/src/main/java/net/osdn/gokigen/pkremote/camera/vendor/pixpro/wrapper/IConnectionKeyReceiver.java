package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper;

import androidx.annotation.NonNull;

public interface IConnectionKeyReceiver
{
    void receivedPassword(@NonNull String password);
    void receivedKeyString(@NonNull byte[] keyString);
}
