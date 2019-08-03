package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper;

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
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.IPanasonicInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.PanasonicCameraCaptureControl;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.PanasonicCameraFocusControl;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.PanasonicCameraZoomLensControl;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.connection.PanasonicCameraConnection;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener.CameraEventObserver;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener.ICameraEventObserver;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener.ICameraStatusHolder;

public class PanasonicCameraWrapper implements IPanasonicCameraHolder, IPanasonicInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();
    private final Activity context;
    private static final int TIMEOUT_MS = 3000;
    private final ICameraStatusReceiver provider;
    private final ICameraChangeListener listener;
    private IPanasonicCamera panasonicCamera = null;
    //private IPanasonicCameraApi panasonicCameraApi = null;
    private ICameraEventObserver eventObserver = null;
    private PanasonicLiveViewControl liveViewControl = null;
    private PanasonicCameraFocusControl focusControl = null;
    private PanasonicCameraCaptureControl captureControl = null;
    private PanasonicCameraZoomLensControl zoomControl = null;
    private PanasonicCameraConnection cameraConnection = null;

    public PanasonicCameraWrapper(final Activity context, final ICameraStatusReceiver statusReceiver , final @NonNull ICameraChangeListener listener)
    {
        this.context = context;
        this.provider = statusReceiver;
        this.listener = listener;
    }

    @Override
    public void prepare()
    {
        Log.v(TAG, " prepare : " + panasonicCamera.getFriendlyName() + " " + panasonicCamera.getModelName());
        try
        {
            //this.panasonicCameraApi = PanasonicCameraApi.newInstance(panasonicCamera);
            if (eventObserver == null)
            {
                eventObserver = CameraEventObserver.newInstance(context, panasonicCamera);
            }
            if (liveViewControl == null)
            {
                liveViewControl = new PanasonicLiveViewControl(panasonicCamera);
            }
            focusControl.setCamera(panasonicCamera);
            captureControl.setCamera(panasonicCamera);
            zoomControl.setCamera(panasonicCamera);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void startRecMode()
    {
        try
        {
            // 撮影モード(RecMode)に切り替え
            String reply = SimpleHttpClient.httpGet(this.panasonicCamera.getCmdUrl() + "cam.cgi?mode=camcmd&value=recmode", TIMEOUT_MS);
            if (!reply.contains("ok"))
            {
                Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE RECMODE.");
            }

            //  フォーカスに関しては、１点に切り替える（仮）
            reply = SimpleHttpClient.httpGet(this.panasonicCamera.getCmdUrl() + "cam.cgi?mode=setsetting&type=afmode&value=1area", TIMEOUT_MS);
            if (!reply.contains("ok"))
            {
                Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE AF MODE 1area.");
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
                if (holder != null)
                {
                    holder.getLiveviewStatus();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void detectedCamera(@NonNull IPanasonicCamera camera)
    {
        Log.v(TAG, "detectedCamera()");
        panasonicCamera = camera;
    }

    @Override
    public ICameraConnection getPanasonicCameraConnection()
    {
        // PanasonicCameraConnectionは複数生成しない。
        if (cameraConnection == null)
        {
            cameraConnection = new PanasonicCameraConnection(context, provider, this, listener);
        }
        return (cameraConnection);
    }

    @Override
    public ILiveViewControl getPanasonicLiveViewControl()
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
    public IPanasonicCamera getPanasonicCamera()
    {
        return (panasonicCamera);
    }

    @Override
    public void injectDisplay(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator, @NonNull IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");

        focusControl = new PanasonicCameraFocusControl(frameDisplayer, indicator);
        captureControl = new PanasonicCameraCaptureControl(frameDisplayer, indicator);
        zoomControl = new PanasonicCameraZoomLensControl();
    }
}
