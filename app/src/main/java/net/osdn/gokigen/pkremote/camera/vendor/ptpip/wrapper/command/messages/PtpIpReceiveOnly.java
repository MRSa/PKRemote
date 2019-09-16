package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;

public class PtpIpReceiveOnly extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;
    private final int id;

    public PtpIpReceiveOnly(int id, @NonNull IPtpIpCommandCallback callback)
    {
        this.callback = callback;
        this.id = id;
    }

    @Override
    public IPtpIpCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (id);
    }

    @Override
    public byte[] commandBody()
    {
        return (null);
    }
}
