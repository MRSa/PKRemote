package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command;

public interface IPixproCommandCallback
{
    void receivedMessage(int id, byte[] rx_body);
}
