package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingModeNotify;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IDisplayInjector;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraInformation;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.sony.ISonyInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.sony.operation.SonyCameraCaptureControl;
import net.osdn.gokigen.pkremote.camera.vendor.sony.operation.SonyCameraFocusControl;
import net.osdn.gokigen.pkremote.camera.vendor.sony.operation.SonyCameraZoomLensControl;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.connection.SonyCameraConnection;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.eventlistener.CameraEventObserver;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.eventlistener.ICameraEventObserver;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.eventlistener.ICameraStatusHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SonyCameraWrapper implements ISonyCameraHolder, ISonyInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();
    private final Activity context;
    private final ICameraStatusReceiver provider;
    private final ICameraChangeListener listener;
    private ISonyCamera sonyCamera = null;
    private ISonyCameraApi sonyCameraApi = null;
    private ICameraEventObserver eventObserver = null;
    private SonyLiveViewControl liveViewControl = null;
    private SonyCameraFocusControl focusControl = null;
    private SonyCameraCaptureControl captureControl = null;
    private SonyCameraZoomLensControl zoomControl = null;

    public SonyCameraWrapper(final Activity context, final ICameraStatusReceiver statusReceiver , final @NonNull ICameraChangeListener listener)
    {
        this.context = context;
        this.provider = statusReceiver;
        this.listener = listener;
    }

    @Override
    public void prepare()
    {
        Log.v(TAG, " prepare : " + sonyCamera.getFriendlyName() + " " + sonyCamera.getModelName());
        try
        {
            this.sonyCameraApi = SonyCameraApi.newInstance(sonyCamera);
            eventObserver = CameraEventObserver.newInstance(context, sonyCameraApi);
            liveViewControl = new SonyLiveViewControl(sonyCameraApi);

            focusControl.setCameraApi(sonyCameraApi);
            captureControl.setCameraApi(sonyCameraApi);
            zoomControl.setCameraApi(sonyCameraApi);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void startRecMode()
    {
        try {
            List<String> apiCommands = getApiCommands();
            int index = apiCommands.indexOf("startRecMode");
            if (index > 0)
            {
                // startRecMode発行
                Log.v(TAG, "----- THIS CAMERA NEEDS COMMAND 'startRecMode'.");
                sonyCameraApi.startRecMode();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    @Override
    public void startEventWatch(@Nullable ICameraChangeListener listener)
    {
        try
        {
            if (eventObserver != null)
            {
                if (listener != null)
                {
                    eventObserver.setEventListener(listener);
                }
                eventObserver.activate();
                eventObserver.start();
                ICameraStatusHolder holder = eventObserver.getCameraStatusHolder();
                holder.getLiveviewStatus();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void detectedCamera(@NonNull ISonyCamera camera)
    {
        Log.v(TAG, "detectedCamera()");
        sonyCamera = camera;
    }

    @Override
    public ICameraConnection getSonyCameraConnection()
    {
        return (new SonyCameraConnection(context, provider, this, listener));
    }

    @Override
    public ILiveViewControl getSonyLiveViewControl()
    {
        return (liveViewControl);
    }

    @Override
    public ILiveViewListener getLiveViewListener()
    {
        return (liveViewControl.getLiveViewListener());
    }

    @Override
    public IFocusingControl getFocusingControl()
    {
        return (focusControl);
    }

    @Override
    public ICameraInformation getCameraInformation()
    {
        return null;
    }

    @Override
    public IZoomLensControl getZoomLensControl()
    {
        return (zoomControl);
    }

    @Override
    public ICaptureControl getCaptureControl()
    {
        return (captureControl);
    }

    @Override
    public IDisplayInjector getDisplayInjector()
    {
        return (this);
    }

    @Override
    public List<String> getApiCommands()
    {
        List<String> availableApis = new ArrayList<>();
        try
        {
            String apiList = sonyCameraApi.getAvailableApiList().getString("result");
            apiList = apiList.replace("[","").replace("]", "").replace("\"","");
            String[] apiListSplit = apiList.split(",");
            availableApis = Arrays.asList(apiListSplit);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (availableApis);
    }

    @Override
    public ISonyCameraApi getCameraApi()
    {
        return (sonyCameraApi);
    }

    @Override
    public void injectDisplay(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator, @NonNull IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");

        focusControl = new SonyCameraFocusControl(frameDisplayer, indicator);
        captureControl = new SonyCameraCaptureControl(frameDisplayer, indicator);
        zoomControl = new SonyCameraZoomLensControl();
    }
}
