package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command;

import androidx.annotation.NonNull;

public interface IPixproCommandPublisher
{
    boolean isConnected();
    boolean enqueueCommand(@NonNull IPixproCommand command);

    void start();
    void stop();
}
