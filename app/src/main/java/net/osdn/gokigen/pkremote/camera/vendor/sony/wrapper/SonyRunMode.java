package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;

import org.json.JSONObject;


public class SonyRunMode implements ICameraRunMode
{
    private final String TAG = toString();
    private ISonyCameraApi cameraApi = null;

    void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi)
    {
        cameraApi = sonyCameraApi;
    }

    @Override
    public void changeRunMode(boolean isRecording)
    {
        try
        {
            if (cameraApi == null)
            {
                return;
            }
            JSONObject reply = cameraApi.setCameraFunction((isRecording)? "Remote Shooting" : "Contents Transfer");
            try
            {
                int value = reply.getInt("result");
                Log.v(TAG, "CHANGE RUN MODE : " + value);
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRecordingMode()
    {
        return (false);
    }
}
