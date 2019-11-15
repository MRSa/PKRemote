package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.connection;

import android.app.Activity;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.nikon.INikonInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommunication;


class NikonCameraDisconnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final Activity activity;
    private final IPtpIpCommunication command;
    private final IPtpIpCommunication async;
    private final IPtpIpCommunication liveview;

    NikonCameraDisconnectSequence(Activity activity, @NonNull INikonInterfaceProvider interfaceProvider)
    {
        this.activity = activity;
        this.command = interfaceProvider.getCommandCommunication();
        this.async = interfaceProvider.getAsyncEventCommunication();
        this.liveview = interfaceProvider.getLiveviewCommunication();
    }

    @Override
    public void run()
    {
        try
        {
            liveview.disconnect();
            async.disconnect();
            command.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
