package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command;

public interface IPtpIpCommandCallback
{
    void receivedMessage(int id, byte[] rx_body);
    void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body);
    boolean isReceiveMulti();
}
