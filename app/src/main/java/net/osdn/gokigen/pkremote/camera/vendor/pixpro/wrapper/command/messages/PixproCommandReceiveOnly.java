package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages;


import androidx.annotation.Nullable;

import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommandCallback;

public class PixproCommandReceiveOnly extends PixproCommandBase
{
    private final IPixproCommandCallback callback;
    private final int id;

    public PixproCommandReceiveOnly(int id, @Nullable IPixproCommandCallback callback)
    {
        this.callback = callback;
        this.id = id;
    }

    @Override
    public IPixproCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (id);
    }

    @Override
    public byte[] commandBody()
    {
        return (null);
    }

    @Override
    public boolean dumpLog()
    {
        return (false);
    }

}
