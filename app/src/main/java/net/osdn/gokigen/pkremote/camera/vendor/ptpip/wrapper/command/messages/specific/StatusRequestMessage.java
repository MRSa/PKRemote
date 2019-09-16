package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific;


import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandBase;

public class StatusRequestMessage extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;

    public StatusRequestMessage(@NonNull IPtpIpCommandCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public IPtpIpCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (SEQ_STATUS_REQUEST);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[] {

                // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                (byte)0x01, (byte)0x00,

                // message_header.type : single_part (0x1015) : 0xd212 (status_request)
                (byte)0x15, (byte)0x10,

                // sequence number
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,

                // data ...
                (byte)0x12, (byte)0xd2, (byte)0x00, (byte)0x00,
        });
    }

    @Override
    public boolean dumpLog()
    {
        return (false);
    }
}
