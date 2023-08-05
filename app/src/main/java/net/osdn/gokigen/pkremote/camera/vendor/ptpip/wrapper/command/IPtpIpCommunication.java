package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command;

import androidx.annotation.NonNull;

public interface IPtpIpCommunication
{
    boolean connect(@NonNull String ipAddress, int portNumber);
    void disconnect();
}
