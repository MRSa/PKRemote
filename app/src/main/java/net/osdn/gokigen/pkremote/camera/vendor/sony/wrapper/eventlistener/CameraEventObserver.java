package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.eventlistener;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraApi;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 *
 */
public class CameraEventObserver implements ICameraEventObserver
{
    private static final String TAG = CameraEventObserver.class.getSimpleName();
    private boolean isEventMonitoring;
    private boolean isActive;

    private final ISonyCameraApi remoteApi;
    private final ReplyJsonParser replyParser;
    private String eventVersion = "1.1";  // 初期値を "1.0" から "1.1" に更新

    public static ICameraEventObserver newInstance(@NonNull Context context, @NonNull ISonyCameraApi apiClient)
    {
        return (new CameraEventObserver(context, apiClient));
    }

    private CameraEventObserver(@NonNull Context context, @NonNull ISonyCameraApi apiClient)
    {
        super();
        remoteApi = apiClient;
        replyParser = new ReplyJsonParser(new Handler(context.getMainLooper()));
        isEventMonitoring = false;
        isActive = false;
    }

    @Override
    public boolean start()
    {
        if (!isActive)
        {
            Log.w(TAG, "start() observer is not active.");
            return (false);
        }

        if (isEventMonitoring)
        {
            Log.w(TAG, "start() already starting.");
            return (false);
        }

        isEventMonitoring = true;
        try
        {
            Thread thread = new Thread()
            {
                @Override
                public void run()
                {
                    Log.d(TAG, "start() exec.");
                    boolean firstCall = true;
                    MONITORLOOP: while (isEventMonitoring)
                    {
                        // At first, call as non-Long Polling.
                        boolean longPolling = !firstCall;
                        try
                        {
                            // Call getEvent API.
                            JSONObject replyJson = remoteApi.getEvent(eventVersion, longPolling);

                            // Check error code at first.
                            int errorCode = findErrorCode(replyJson);
                            Log.d(TAG, "getEvent errorCode: " + errorCode);
                            switch (errorCode) {
                                case 0: // no error
                                    // Pass through.
                                    break;
                                case 1: // "Any" error
                                case 12: // "No such method" error
                                    if (eventVersion.equals("1.1"))
                                    {
                                        // "1.1" でエラーが発生した時には "1.0" にダウングレードして再実行
                                        eventVersion = "1.0";
                                        continue MONITORLOOP;
                                    }
                                    replyParser.fireResponseErrorListener();
                                    break MONITORLOOP; // end monitoring.

                                case 2: // "Timeout" error
                                    // Re-call immediately.
                                    continue MONITORLOOP;

                                case 40402: // "Already polling" error
                                    // Retry after 5 sec.
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        // do nothing.
                                    }
                                    continue MONITORLOOP;

                                default:
                                    Log.w(TAG, "SimpleCameraEventObserver: Unexpected error: " + errorCode);
                                    replyParser.fireResponseErrorListener();
                                    break MONITORLOOP; // end monitoring.
                            }

                            //  parse
                            replyParser.parse(replyJson);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        firstCall = false;
                    } // MONITORLOOP end.
                    isEventMonitoring = false;
                }
            };
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (true);
    }

    @Override
    public void stop()
    {
        isEventMonitoring = false;
    }

    @Override
    public void release()
    {
        isEventMonitoring = false;
        isActive = false;
    }

    @Override
    public void setEventListener(@NonNull ICameraChangeListener listener)
    {
        try
        {
            replyParser.setEventChangeListener(listener);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void clearEventListener()
    {
        try
        {
            replyParser.clearEventChangeListener();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public ICameraStatusHolder getCameraStatusHolder()
    {
        return (replyParser);
    }

    @Override
    public void activate()
    {
        isActive = true;
    }

    private static int findErrorCode(JSONObject replyJson)
    {
        int code = 0; // 0 means no error.
        try
        {
            if (replyJson.has("error"))
            {
                JSONArray errorObj = replyJson.getJSONArray("error");
                code = errorObj.getInt(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            code = -1;
        }
        return (code);
    }
}
