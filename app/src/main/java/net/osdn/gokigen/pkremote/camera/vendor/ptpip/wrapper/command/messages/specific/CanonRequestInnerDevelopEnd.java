package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandBase;

public class CanonRequestInnerDevelopEnd  extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;
    private final boolean isDumpLog;
    private final int id;

    public CanonRequestInnerDevelopEnd(@NonNull IPtpIpCommandCallback callback, boolean isDumpLog, int id)
    {
        this.callback = callback;
        this.isDumpLog = isDumpLog;
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
    public boolean dumpLog()
    {
        return (isDumpLog);
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
                (byte) 0x43, (byte) 0x91,

                 // sequence number
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // ???
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        });
    }

    @Override
    public byte[] commandBody2()
    {
        return (new byte[]{

                // packet type
                (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // sequence number
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // データサイズ
                (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        });
    }

    @Override
    public byte[] commandBody3()
    {
        return (new byte[]{

                // packet type
                (byte) 0x0c, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // sequence number
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // ????
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        });
    }

}
