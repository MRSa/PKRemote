package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages;


import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommand;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages;

public class PtpIpCommandBase implements IPtpIpCommand, IPtpIpMessages
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
    public int embeddedSequenceNumberIndex()
    {
        return (14);
    }

    @Override
    public int embeddedSequenceNumberIndex2()
    {
        return (8);
    }

    @Override
    public int embeddedSequenceNumberIndex3()
    {
        return (8);
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
    public byte[] commandBody3()
    {
        return (null);
    }

    @Override
    public IPtpIpCommandCallback responseCallback()
    {
        return (null);
    }

    @Override
    public boolean dumpLog()
    {
        return (true);
    }
}
