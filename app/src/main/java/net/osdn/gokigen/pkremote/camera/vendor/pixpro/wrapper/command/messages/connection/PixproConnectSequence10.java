package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection;

import androidx.annotation.Nullable;

import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.PixproCommandBase;

import static net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.IPixproMessages.SEQ_CONNECT_10;


public class PixproConnectSequence10 extends PixproCommandBase
{
    private final IPixproCommandCallback callback;

    public PixproConnectSequence10(@Nullable IPixproCommandCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public int getId()
    {
        return SEQ_CONNECT_10;
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[]
                {
                        //   0xed  0x03
                        (byte) 0x2e , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x20 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0xed , (byte) 0x03 , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x80 ,
                        (byte) 0x12 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x20 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x12 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x69 , (byte) 0x08 , (byte) 0x00 , (byte) 0x00 , (byte) 0x69 , (byte) 0x08 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0xff , (byte) 0xff , (byte) 0xff , (byte) 0xff , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00
                });
    }

    @Override
    public IPixproCommandCallback responseCallback()
    {
        return (this.callback);
    }
}
