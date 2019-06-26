package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages;

import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommand;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXMessages;

public class FujiXCommandBase implements IFujiXCommand, IFujiXMessages
{
    @Override
    public int getId()
    {
        return (SEQ_DUMMY);
    }

    @Override
    public boolean receiveAgainShortLengthMessage()
    {
        return (true);
    }

    @Override
    public boolean useSequenceNumber()
    {
        return (true);
    }

    @Override
    public boolean isIncrementSeqNumber()
    {
        return (true);
    }

    @Override
    public int receiveDelayMs()
    {
        return (100);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[12]);
    }

    @Override
    public byte[] commandBody2()
    {
        return (null);
    }

    @Override
    public IFujiXCommandCallback responseCallback()
    {
        return (null);
    }

    @Override
    public boolean dumpLog()
    {
        return (true);
    }
}
