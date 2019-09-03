package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.connection;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCamera;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraHolder;

/**
 *   SONYカメラとの接続処理
 *
 */
public class SonyCameraConnectSequence implements Runnable, SonySsdpClient.ISearchResultCallback
{
    private final String TAG = this.toString();
    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ISonyCameraHolder cameraHolder;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final ICameraChangeListener listener;
    private final SonySsdpClient client;

    SonyCameraConnectSequence(Activity context, ICameraStatusReceiver statusReceiver, final ICameraConnection cameraConnection, final @NonNull ISonyCameraHolder cameraHolder, final @NonNull ICameraChangeListener listener)
    {
        Log.v(TAG, "SonyCameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
        this.cameraHolder = cameraHolder;
        this.listener = listener;
        client = new SonySsdpClient(context, this, statusReceiver, 1);
    }

    @Override
    public void run()
    {
        Log.v(TAG, "search()");
        try
        {
            cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start));
            client.search();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceFound(ISonyCamera cameraDevice)
    {
        try
        {
            cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_detected) + " " + cameraDevice.getFriendlyName());
            cameraHolder.detectedCamera(cameraDevice);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinished()
    {
        Log.v(TAG, "SonyCameraConnectSequence.onFinished()");
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        cameraHolder.prepare();
                        cameraHolder.startPlaybackMode();
                        //cameraHolder.startRecMode();
                        //cameraHolder.startEventWatch(listener);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    Log.v(TAG, "CameraConnectSequence:: connected.");
                    onConnectNotify();
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void onConnectNotify()
    {
        try
        {
            final Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    // カメラとの接続確立を通知する
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected));
                    cameraStatusReceiver.onCameraConnected();
                    Log.v(TAG, "onConnectNotify()");
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void waitForAMoment(long mills)
    {
        if (mills > 0)
        {
            try {
                Log.v(TAG, " WAIT " + mills + "ms");
                Thread.sleep(mills);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onErrorFinished(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }

}
