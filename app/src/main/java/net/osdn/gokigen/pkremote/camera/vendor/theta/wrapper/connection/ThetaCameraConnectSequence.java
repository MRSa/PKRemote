package net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.connection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import org.json.JSONArray;
import org.json.JSONObject;

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
        try
        {
            final String oscInfoUrl = "http://192.168.1.1/osc/info";
            final int TIMEOUT_MS = 5000;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean useThetaV21 = preferences.getBoolean(IPreferencePropertyAccessor.USE_OSC_THETA_V21, false);

            String response = SimpleHttpClient.httpGet(oscInfoUrl, TIMEOUT_MS);
            Log.v(TAG, " " + oscInfoUrl + " " + response);
            if (response.length() > 0)
            {
                try
                {
                    JSONArray apiLevelArray = new JSONObject(response).getJSONArray("apiLevel");
                    int size = apiLevelArray.length();
                    for (int index = 0; index < size; index++)
                    {
                        int api = apiLevelArray.getInt(index);
                        if ((api == 2)&&(useThetaV21))
                        {
                            // API Level V2.1を使用して通信する
                            connectApiV21();
                            return;
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // onConnectError(e.getLocalizedMessage());
        }

        // API Level V2 を使用して通信する
        connectApiV2();
    }

    /**
     *
     */
    private void connectApiV2()
    {
        final String commandsExecuteUrl = "http://192.168.1.1/osc/commands/execute";
        final String startSessionData = "{\"name\":\"camera.startSession\",\"parameters\":{\"timeout\":0}}";
        final String getStateUrl = "http://192.168.1.1/osc/state";
        final int TIMEOUT_MS = 2000;

        try
        {
            String response = SimpleHttpClient.httpPostWithHeader(commandsExecuteUrl, startSessionData, null, "application/json;charset=utf-8", TIMEOUT_MS);
            Log.v(TAG, " " + commandsExecuteUrl + " " + startSessionData + " " + response);

            String response2 = SimpleHttpClient.httpPostWithHeader(getStateUrl, "", null, "application/json;charset=utf-8", TIMEOUT_MS);
            Log.v(TAG, " " + getStateUrl + " " + response2);

            onConnectNotify();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            onConnectError(e.getLocalizedMessage());
        }
    }

    private void connectApiV21()
    {
        final String commandsExecuteUrl = "http://192.168.1.1/osc/commands/execute";
        final String startSessionData = "{\"name\":\"camera.startSession\",\"parameters\":{\"timeout\":0}}";
        final String getStateUrl = "http://192.168.1.1/osc/state";
        final int TIMEOUT_MS = 5000;

        try
        {
            String responseS = SimpleHttpClient.httpPostWithHeader(commandsExecuteUrl, startSessionData, null, "application/json;charset=utf-8", TIMEOUT_MS);
            Log.v(TAG, " " + commandsExecuteUrl + " " + startSessionData + " " + responseS);

            String response = SimpleHttpClient.httpPostWithHeader(getStateUrl, "", null, "application/json;charset=utf-8", TIMEOUT_MS);
            Log.v(TAG, " " + getStateUrl + " " + response);
            if (response.length() > 0)
            {
                int apiLevel = 1;
               JSONObject object = new JSONObject(response);
               try
               {
                   apiLevel = object.getJSONObject("state").getInt("_apiVersion");
               }
               catch (Exception e)
               {
                   e.printStackTrace();
               }
               if (apiLevel != 2)
               {
                   JSONObject jsonObject = object.getJSONObject("state");
                   String sessionId = jsonObject.getString("sessionId");
                   String setApiLevelData = "{\"name\":\"camera.setOptions\",\"parameters\":{" + "\"sessionId\" : \"" + sessionId + "\", \"options\":{ \"clientVersion\":2}}}";

                   String response3 = SimpleHttpClient.httpPostWithHeader(commandsExecuteUrl, setApiLevelData, null, "application/json;charset=utf-8", TIMEOUT_MS);
                   Log.v(TAG, " " + commandsExecuteUrl + " " + setApiLevelData + " " + response3);
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

    private void onConnectError(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }

}
