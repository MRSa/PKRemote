package net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper;

import android.util.Log;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

public class OlympusPenRunMode implements ICameraRunMode
{
    private final String TAG = this.toString();

    @Override
    public void changeRunMode(final boolean isRecording)
    {
        final int TIMEOUT_MS = 5000;
        try
        {
            final Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    // カメラとの接続確立を通知する
                    Map<String, String> headerMap = new HashMap<>();
                    headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
                    headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

                    String playModeUrl = "http://192.168.0.10/switch_cameramode.cgi";
                    if (isRecording)
                    {
                        playModeUrl = playModeUrl + "?mode=rec";
                    }
                    else
                    {
                        playModeUrl = playModeUrl + "?mode=play";
                    }
                    String response = SimpleHttpClient.httpGetWithHeader(playModeUrl, headerMap, null, TIMEOUT_MS);
                    Log.v(TAG, " " + playModeUrl + " " + response);
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRecordingMode()
    {
        return (true);
    }
}
