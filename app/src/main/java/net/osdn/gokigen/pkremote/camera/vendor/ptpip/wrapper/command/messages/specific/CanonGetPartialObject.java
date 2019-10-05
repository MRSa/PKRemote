package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandBase;

public class CanonGetPartialObject extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;
    private final boolean isDumpLog;
    private final int id;
    private final int holdId;

    private final byte data00;
    private final byte data01;
    private final byte data02;
    private final byte data03;

    private final byte data10;
    private final byte data11;
    private final byte data12;
    private final byte data13;

    public CanonGetPartialObject(@NonNull IPtpIpCommandCallback callback, int id, boolean isDumpLog, int holdId, int startPosition, int dataSize)
    {
        this.callback = callback;
        this.isDumpLog = isDumpLog;
        this.id = id;
        this.holdId = holdId;

        data00 = ((byte) (0x000000ff & startPosition));
        data01 = ((byte)((0x0000ff00 & startPosition) >> 8));
        data02 = ((byte)((0x00ff0000 & startPosition) >> 16));
        data03 = ((byte)((0xff000000 & startPosition) >> 24));

        data10 = ((byte) (0x000000ff & dataSize));
        data11 = ((byte)((0x0000ff00 & dataSize) >> 8));
        data12 = ((byte)((0x00ff0000 & dataSize) >> 16));
        data13 = ((byte)((0xff000000 & dataSize) >> 24));
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
    public boolean dumpLog()
    {
        return (isDumpLog);
    }

    @Override
    public int receiveDelayMs()
    {
        return (35);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[]{
                // packet type
                (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // data phase info
                (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // operation code
                (byte) 0x07, (byte) 0x91,

                // sequence number
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // ??? (0x01)
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // data size
                data00, data01, data02, data03,

                // data size
                data10, data11, data12, data13,
        });
    }

    @Override
    public int getHoldId()
    {
        return (holdId);
    }

    @Override
    public boolean isHold()
    {
        return (true);
    }

    @Override
    public boolean isRelease()
    {
        return (false);
    }

}
