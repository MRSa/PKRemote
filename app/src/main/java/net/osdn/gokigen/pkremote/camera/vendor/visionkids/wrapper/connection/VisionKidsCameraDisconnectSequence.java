package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.connection;

import android.util.Log;

import androidx.annotation.NonNull;

public class VisionKidsCameraDisconnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final IVisionKidsConnection connection;

    VisionKidsCameraDisconnectSequence(@NonNull final IVisionKidsConnection connection)
    {
        this.connection = connection;
    }

    @Override
    public void run()
    {
        // なにもしない（というかできない）
        Log.v(TAG, " Power off (VisionKids)");
        connection.forceDisconnect();
    }
}
