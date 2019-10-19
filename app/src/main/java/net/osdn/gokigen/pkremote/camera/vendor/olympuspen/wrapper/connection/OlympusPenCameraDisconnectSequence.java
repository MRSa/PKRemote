package net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper.connection;

import android.app.Activity;
import android.util.Log;

import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

public class OlympusPenCameraDisconnectSequence  implements Runnable
{
    private final String TAG = this.toString();
    private final Activity activity;
    private final boolean powerOff;

    OlympusPenCameraDisconnectSequence(Activity activity, boolean isOff)
    {
        this.activity = activity;
        this.powerOff = isOff;
    }

    @Override
    public void run()
    {
        // カメラをPowerOffして接続を切る
        try
        {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
            headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

            if (powerOff)
            {
                final String cameraPowerOffUrl = "http://192.168.0.10/exec_pwoff.cgi";
                final int TIMEOUT_MS = 5000;
                String response = SimpleHttpClient.httpGetWithHeader(cameraPowerOffUrl, headerMap, null, TIMEOUT_MS);
                Log.v(TAG, " " + cameraPowerOffUrl + " " + response);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
