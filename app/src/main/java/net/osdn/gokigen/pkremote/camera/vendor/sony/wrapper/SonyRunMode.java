package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.IInformationReceiver;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;

public class SonyRunMode implements ICameraRunMode
{
    //private final String TAG = toString();
    private final IInformationReceiver informationReceiver;
    private ISonyCameraApi cameraApi = null;

    SonyRunMode(@NonNull IInformationReceiver informationReceiver)
    {
        this.informationReceiver = informationReceiver;
    }

    void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi)
    {
        cameraApi = sonyCameraApi;
    }

    @Override
    public void changeRunMode(boolean isRecording)
    {
/*
        try
        {
            if (cameraApi == null)
            {
                return;
            }

            boolean isAvailable = false;
            int maxRetryCount = 5;    // 最大リトライ回数
            while ((!isAvailable)&&(maxRetryCount > 0))
            {
                isAvailable = setCameraFunction(isRecording);
                maxRetryCount--;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
*/
    }

/*
    private boolean setCameraFunction(boolean isRecording)
    {
        try
        {
             JSONObject reply = cameraApi.setCameraFunction((isRecording) ? "Remote Shooting" : "Contents Transfer");
            try
            {
                int value = reply.getInt("result");
                Log.v(TAG, "CHANGE RUN MODE : " + value);
                return (true);
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
                informationReceiver.updateMessage("RETRY...", false, false, 0);
                Thread.sleep(1500); //  1500ms 待つ
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }
*/

    @Override
    public boolean isRecordingMode()
    {
        return (false);
    }
}
