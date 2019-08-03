package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.connection;


public class PanasonicCameraDisconnectSequence implements Runnable
{
    //private final String TAG = this.toString();
    //private final boolean powerOff;

    PanasonicCameraDisconnectSequence(boolean isOff)
    {
        //this.powerOff = isOff;
    }

    @Override
    public void run()
    {
        // カメラをPowerOffして接続を切る
    }
}
