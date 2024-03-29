package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.connection;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommunication;


class CanonCameraDisconnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final AppCompatActivity activity;
    private final IPtpIpCommunication command;
    private final IPtpIpCommunication async;
    private final IPtpIpCommunication liveview;

    CanonCameraDisconnectSequence(AppCompatActivity activity, @NonNull IPtpIpInterfaceProvider interfaceProvider)
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
            Log.v(TAG, " disconnect");
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
