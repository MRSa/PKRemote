package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command;

public interface IFujiXCommandCallback
{
    void receivedMessage(int id, byte[] rx_body);
}
