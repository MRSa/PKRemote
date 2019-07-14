package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command;

public interface IFujiXCommandCallback
{
    void receivedMessage(int id, byte[] rx_body);
    void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body);
    boolean isReceiveMulti();
}
