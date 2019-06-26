package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;

public class ReceiveOnly extends FujiXCommandBase
{
    private final IFujiXCommandCallback callback;

    public ReceiveOnly(@NonNull IFujiXCommandCallback callback)
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
        return (SEQ_START_2ND_RECEIVE);
    }

    @Override
    public byte[] commandBody()
    {
        return (null);
    }
}
