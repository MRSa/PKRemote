package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandBase;

public class CanonInitEventRequest extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;
    private final int connectionNumber;

    public CanonInitEventRequest(@NonNull IPtpIpCommandCallback callback, int connectionNumber)
    {
        this.callback = callback;
        this.connectionNumber = connectionNumber;
    }

    @Override
    public IPtpIpCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (SEQ_EVENT_INITIALIZE);
    }

    @Override
    public int receiveDelayMs()
    {
        return (100);
    }

    @Override
    public boolean receiveAgainShortLengthMessage()
    {
        return (false);
    }

    @Override
    public boolean useSequenceNumber()
    {
        return (false);
    }

    @Override
    public boolean isIncrementSeqNumber()
    {
        return (false);
    }

    @Override
    public byte[] commandBody()
    {
        byte data0 = ((byte) (0x000000ff & connectionNumber));
        byte data1 = ((byte)((0x0000ff00 & connectionNumber) >> 8));
        byte data2 = ((byte)((0x00ff0000 & connectionNumber) >> 16));
        byte data3 = ((byte)((0xff000000 & connectionNumber) >> 24));

        return (new byte[] {
                // packet type
                (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00, data0, data1, data2, data3,
        });
    }
}
