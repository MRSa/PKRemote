package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener;
import net.osdn.gokigen.pkremote.camera.liveview.CameraLiveViewListenerImpl;
import net.osdn.gokigen.pkremote.camera.utils.SimpleLiveviewSlicer;

import org.json.JSONArray;
import org.json.JSONObject;

public class SonyLiveViewControl implements ILiveViewControl
{
    private final String TAG = toString();
    private final ISonyCameraApi cameraApi;
    //private final BlockingQueue<byte[]> mJpegQueue = new ArrayBlockingQueue<>(2);
    private final CameraLiveViewListenerImpl liveViewListener;
    private boolean whileFetching = false;
    private static final int FETCH_ERROR_MAX = 30;

    SonyLiveViewControl(@NonNull ISonyCameraApi cameraApi)
    {
        this.cameraApi = cameraApi;
        liveViewListener = new CameraLiveViewListenerImpl();
    }

    @Override
    public void changeLiveViewSize(String size)
    {

    }

    @Override
    public void startLiveView(boolean isCameraScreen)
    {
        Log.v(TAG, "startLiveView() : " + isCameraScreen);
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        JSONObject replyJson;
                        replyJson = cameraApi.startLiveview();
                        if (!SonyCameraApi.isErrorReply(replyJson))
                        {
                            try
                            {
                                JSONArray resultsObj = replyJson.getJSONArray("result");
                                if (1 <= resultsObj.length())
                                {
                                    // Obtain liveview URL from the result.
                                    final String liveviewUrl = resultsObj.getString(0);
                                    start(liveviewUrl);
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
                    }
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
    public void stopLiveView()
    {
        Log.v(TAG, "stopLiveView()");
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        JSONObject resultsObj = cameraApi.stopLiveview();
                        if (resultsObj == null)
                        {
                            Log.v(TAG, "stopLiveview() reply is null.");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
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
    public void updateDigitalZoom()
    {

    }

    @Override
    public void updateMagnifyingLiveViewScale(boolean isChangeScale)
    {

    }

    @Override
    public float getMagnifyingLiveViewScale()
    {
        return (1.0f);
    }

    @Override
    public float getDigitalZoomScale()
    {
        return (1.0f);
    }



    public boolean start(final String streamUrl)
    {
        if (streamUrl == null)
        {
            Log.e(TAG, "start() streamUrl is null.");
            return (false);
        }
        if (whileFetching)
        {
            Log.v(TAG, "start() already starting.");
        }
        whileFetching = true;

        // A thread for retrieving liveview data from server.
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.d(TAG, "Starting retrieving streaming data from server.");
                    SimpleLiveviewSlicer slicer = null;
                    int continuousNullDataReceived = 0;
                    try
                    {
                        // Create Slicer to open the stream and parse it.
                        slicer = new SimpleLiveviewSlicer();
                        slicer.open(streamUrl);

                        while (whileFetching)
                        {
                            final SimpleLiveviewSlicer.Payload payload = slicer.nextPayload();
                            if (payload == null)
                            {
                                //Log.v(TAG, "Liveview Payload is null.");
                                continuousNullDataReceived++;
                                if (continuousNullDataReceived > FETCH_ERROR_MAX)
                                {
                                    Log.d(TAG, " FETCH ERROR MAX OVER ");
                                    break;
                                }
                                continue;
                            }
                            //if (mJpegQueue.size() == 2)
                            //{
                            //    mJpegQueue.remove();
                            //}
                            //mJpegQueue.add(payload.getJpegData());
                            liveViewListener.onUpdateLiveView(payload.getJpegData(), null);
                            continuousNullDataReceived = 0;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            if (slicer != null)
                            {
                                slicer.close();
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        //mJpegQueue.clear();
                        if ((!whileFetching)&&(continuousNullDataReceived > FETCH_ERROR_MAX))
                        {
                            // 再度ライブビューのスタートをやってみる。
                            whileFetching = false;
                            //continuousNullDataReceived = 0;
                            start(streamUrl);
                        }
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (true);
    }

    public ILiveViewListener getLiveViewListener()
    {
        return (liveViewListener);
    }
}
