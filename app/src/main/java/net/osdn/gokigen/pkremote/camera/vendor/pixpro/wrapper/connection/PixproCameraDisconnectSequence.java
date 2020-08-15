package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.connection;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.pixpro.IPixproInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommunication;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.status.PixproStatusChecker;

class PixproCameraDisconnectSequence implements Runnable
{
    //private final String TAG = this.toString();
    private final IPixproCommunication command;
    private final PixproStatusChecker statusChecker;

    PixproCameraDisconnectSequence(@NonNull IPixproInterfaceProvider interfaceProvider, @NonNull PixproStatusChecker statusChecker)
    {
        this.command = interfaceProvider.getCommandCommunication();
        this.statusChecker = statusChecker;
    }

    @Override
    public void run()
    {
        try
        {
            statusChecker.stopStatusWatch();
            command.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
