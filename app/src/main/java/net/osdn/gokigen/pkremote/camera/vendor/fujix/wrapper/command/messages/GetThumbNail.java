package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;

public class GetThumbNail extends FujiXCommandBase
{
    private final IFujiXCommandCallback callback;
    private final byte lower;
    private final byte upper;

    public GetThumbNail(int indexNumber, @NonNull IFujiXCommandCallback callback)
    {
        this.lower = ((byte) (0x000000ff & indexNumber));
        this.upper = ((byte)((0x0000ff00 & indexNumber) >> 8));
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
        return (SEQ_THUMBNAIL);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[] {

                // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                (byte)0x01, (byte)0x00,

                // message_header.type : ThumbNail (0x100a)
                (byte)0x0a, (byte)0x10,

                // sequence number
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,

                // data ...
                lower, upper, (byte)0x00, (byte)0x00,
        });
    }
    @Override
    public boolean dumpLog()
    {
        return (false);
    }
}
