package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;

public class FocusUnlock extends FujiXCommandBase
{
    private final IFujiXCommandCallback callback;

    public FocusUnlock(@NonNull IFujiXCommandCallback callback)
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
        return (SEQ_FOCUS_UNLOCK);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[] {

                // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                (byte)0x01, (byte)0x00,

                // message_header.type : focus_unlock (0x9027)
                (byte)0x27, (byte)0x90,

                // sequence number
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        });
    }
}
