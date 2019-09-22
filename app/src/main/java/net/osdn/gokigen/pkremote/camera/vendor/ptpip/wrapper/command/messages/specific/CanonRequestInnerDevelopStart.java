package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandBase;

public class CanonRequestInnerDevelopStart  extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;
    private final boolean isDumpLog;
    private final int id;

    private final byte data0;
    private final byte data1;
    private final byte data2;
    private final byte data3;

    public CanonRequestInnerDevelopStart(@NonNull IPtpIpCommandCallback callback, boolean isDumpLog, int id, int objectId)
    {
        this.callback = callback;
        this.isDumpLog = isDumpLog;
        this.id = id;

        data0 = ((byte) (0x000000ff & objectId));
        data1 = ((byte)((0x0000ff00 & objectId) >> 8));
        data2 = ((byte)((0x00ff0000 & objectId) >> 16));
        data3 = ((byte)((0xff000000 & objectId) >> 24));
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
                (byte) 0x41, (byte) 0x91,

                 // sequence number
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // object id
                data0, data1, data2, data3,

                // ???
                (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00,
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
                (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00,
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

                // data
                (byte) 0x0f, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        });
    }

}
