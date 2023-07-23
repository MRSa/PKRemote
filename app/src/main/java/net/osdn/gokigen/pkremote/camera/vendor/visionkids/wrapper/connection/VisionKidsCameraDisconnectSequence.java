package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.connection;

import android.util.Log;

public class VisionKidsCameraDisconnectSequence implements Runnable
{
    private final String TAG = this.toString();

    VisionKidsCameraDisconnectSequence()
    {
        // なにもしない
    }

    @Override
    public void run()
    {
        // なにもしない（というかできない）
        Log.v(TAG, " Power off (VisionKids)");
    }
}
