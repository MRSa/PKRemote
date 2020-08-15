package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages;


import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommand;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommandCallback;

import static net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.IPixproMessages.SEQ_DUMMY;

public class PixproCommandBase  implements IPixproCommand
{
    @Override
    public int getId()
    {
        return SEQ_DUMMY;
    }

    @Override
    public int receiveDelayMs()
    {
        return (30);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[0]);
    }

    @Override
    public byte[] commandBody2()
    {
        return (null);
    }

    @Override
    public int maxRetryCount()
    {
        return (50);
    }

    @Override
    public boolean sendRetry()
    {
        return (false);
    }

    @Override
    public IPixproCommandCallback responseCallback()
    {
        return (null);
    }

    @Override
    public boolean dumpLog()
    {
        return (false);
    }
}
