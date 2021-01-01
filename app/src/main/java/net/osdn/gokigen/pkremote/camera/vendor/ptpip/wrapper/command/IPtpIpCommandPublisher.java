package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command;

import androidx.annotation.NonNull;

public interface IPtpIpCommandPublisher
{
    boolean isConnected();
    boolean enqueueCommand(@NonNull IPtpIpCommand command);

    boolean flushHoldQueue();

    int isExistCommandMessageQueue(int id);
    int getCurrentQueueSize();
    boolean flushQueue();

    void start();
    void stop();
}
