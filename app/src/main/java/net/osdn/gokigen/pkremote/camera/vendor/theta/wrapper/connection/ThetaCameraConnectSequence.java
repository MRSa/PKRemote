package net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.connection;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;

/**
 *   Thetaとの接続シーケンス
 *
 */
public class ThetaCameraConnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;

    ThetaCameraConnectSequence(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection)
    {
        Log.v(TAG, "ThetaCameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
    }

    @Override
    public void run()
    {
        final String oscInfoUrl = "http://192.168.1.1/osc/info";
        final String commandsExecuteUrl = "http://192.168.1.1/osc/commands/execute";
        final String startSessionData = "{\"name\":\"camera.startSession\",\"parameters\":{\"timeout\":0}}";
        final String getStateUrl = "http://192.168.1.1/osc/state";

        final int TIMEOUT_MS = 5000;
        try
        {
            String response = SimpleHttpClient.httpGet(oscInfoUrl, TIMEOUT_MS);
            Log.v(TAG, " " + oscInfoUrl + " " + response);
            if (response.length() > 0)
            {

                String response2 = SimpleHttpClient.httpPost(commandsExecuteUrl, startSessionData, TIMEOUT_MS);
                Log.v(TAG, " " + commandsExecuteUrl + " " + startSessionData + " " + response2);

                String response3 = SimpleHttpClient.httpPost(getStateUrl, "", TIMEOUT_MS);
                Log.v(TAG, " " + getStateUrl + " " + response3);

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

    private void onConnectError(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }

}
