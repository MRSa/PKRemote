package net.osdn.gokigen.pkremote.camera.vendor.ricoh.wrapper.connection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import androidx.annotation.NonNull;

class RicohGr2CameraConnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;

    RicohGr2CameraConnectSequence(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection)
    {
        Log.v(TAG, "RicohGr2CameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
    }

    @Override
    public void run()
    {
        final String areYouThereUrl = "http://192.168.0.1/v1/ping";
        final String grCommandUrl = "http://192.168.0.1/_gr";
        final int TIMEOUT_MS = 5000;
        try
        {
            String response = SimpleHttpClient.httpGet(areYouThereUrl, TIMEOUT_MS);
            Log.v(TAG, areYouThereUrl + " " + response);
            if (response.length() > 0)
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

                // 接続時、レンズロックOFF
                {
                    final String postData = "cmd=acclock off";
                    String response0 = SimpleHttpClient.httpPost(grCommandUrl, postData, TIMEOUT_MS);
                    Log.v(TAG, grCommandUrl + " " + response0);
                }

                // 接続時、カメラの画面を消す
                if (preferences.getBoolean(IPreferencePropertyAccessor.GR2_LCD_SLEEP, false))
                {
                    final String postData = "cmd=lcd sleep on";
                    String response0 = SimpleHttpClient.httpPost(grCommandUrl, postData, TIMEOUT_MS);
                    Log.v(TAG, grCommandUrl + " " + response0);
                }
                onConnectNotify();
            }
            else
            {
                onConnectError(context.getString(R.string.camera_not_found));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            onConnectError(e.getLocalizedMessage());
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

/*
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
*/

    private void onConnectError(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }
}
