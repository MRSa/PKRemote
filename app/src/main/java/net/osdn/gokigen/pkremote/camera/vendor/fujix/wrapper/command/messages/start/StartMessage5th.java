package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start;


import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.FujiXCommandBase;

public class StartMessage5th extends FujiXCommandBase
{
    private final IFujiXCommandCallback callback;

    public StartMessage5th(@NonNull IFujiXCommandCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public IFujiXCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (SEQ_START_5TH);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[] {
                // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                (byte)0x01, (byte)0x00,

                // message_header.type : two_part (0x1016)  : 0xdf24
                (byte)0x16, (byte)0x10,

                // sequence number
                (byte)0x05, (byte)0x00, (byte)0x00, (byte)0x00,

                // data ...
                (byte)0x24, (byte)0xdf, (byte)0x00, (byte)0x00,
        });
    }

    @Override
    public byte[] commandBody2()
    {
        return (new byte[] {
                // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                (byte)0x02, (byte)0x00,

                // message_header.type : two_part (0x1016)  : 0x00020007
                (byte)0x16, (byte)0x10,

                // sequence number
                (byte)0x05, (byte)0x00, (byte)0x00, (byte)0x00,

                // data ...
                (byte)0x07, (byte)0x00, (byte)0x02, (byte)0x00,
        });
    }
}
