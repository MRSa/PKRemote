package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command;

import androidx.annotation.NonNull;

public interface IFujiXCommandPublisher
{
    boolean isConnected();
    boolean enqueueCommand(@NonNull IFujiXCommand command);

    void start();
    void stop();
}
