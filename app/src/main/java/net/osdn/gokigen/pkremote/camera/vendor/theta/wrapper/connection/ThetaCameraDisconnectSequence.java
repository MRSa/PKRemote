package net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.connection;

import android.app.Activity;

public class ThetaCameraDisconnectSequence implements Runnable
{
    private final String TAG = this.toString();

    ThetaCameraDisconnectSequence(Activity activity, boolean isOff)
    {
        //
    }

    @Override
    public void run()
    {
        // カメラをPowerOffして接続を切る
/*
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
*/
    }
}
