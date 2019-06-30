package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingModeNotify;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IDisplayInjector;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraHardwareStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraInformation;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.IFujiXInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.operation.FujiXCaptureControl;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.operation.FujiXFocusingControl;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.operation.FujiXZoomControl;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.FujiXAsyncResponseReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.FujiXCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommunication;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.connection.FujiXConnection;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.liveview.FujiXLiveViewControl;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.status.FujiXStatusChecker;

public class FujiXInterfaceProvider implements IFujiXInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();

    private static final int STREAM_PORT = 55742;
    private static final int ASYNC_RESPONSE_PORT = 55741;
    private static final int CONTROL_PORT = 55740;
    private static final String CAMERA_IP = "192.168.0.1";

    private final Activity activity;
    private final FujiXRunMode runmode;
    private final FujiXHardwareStatus hardwareStatus;
    private FujiXButtonControl fujiXButtonControl;
    private FujiXConnection fujiXConnection;
    private FujiXCommandPublisher commandPublisher;
    private FujiXLiveViewControl liveViewControl;
    private FujiXAsyncResponseReceiver asyncReceiver;
    private FujiXZoomControl zoomControl;
    private FujiXCaptureControl captureControl;
    private FujiXFocusingControl focusingControl;
    private FujiXStatusChecker statusChecker;
    private ICameraStatusUpdateNotify statusListener;
    private FujiXPlaybackControl playbackControl;

    public FujiXInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider, @NonNull ICameraStatusUpdateNotify statusListener)
    {
        this.activity = context;
        commandPublisher = new FujiXCommandPublisher(CAMERA_IP, CONTROL_PORT);
        liveViewControl = new FujiXLiveViewControl(context, CAMERA_IP, STREAM_PORT);
        asyncReceiver = new FujiXAsyncResponseReceiver(CAMERA_IP, ASYNC_RESPONSE_PORT);
        fujiXConnection = new FujiXConnection(context, provider, this);
        zoomControl = new FujiXZoomControl();
        statusChecker = new FujiXStatusChecker(activity, commandPublisher);
        this.statusListener = statusListener;
        this.runmode = new FujiXRunMode();
        this.hardwareStatus = new FujiXHardwareStatus();
        this.fujiXButtonControl = new FujiXButtonControl();
        this.playbackControl = new FujiXPlaybackControl(this);
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        captureControl = new FujiXCaptureControl(commandPublisher, frameDisplayer);
        focusingControl = new FujiXFocusingControl(activity, commandPublisher, frameDisplayer, indicator);
    }

    @Override
    public ICameraConnection getFujiXCameraConnection()
    {
        return (fujiXConnection);
    }

    @Override
    public ILiveViewControl getLiveViewControl()
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
        return (focusingControl);
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
    public IFujiXCommandPublisher getCommandPublisher()
    {
        return (commandPublisher);
    }

    @Override
    public IFujiXCommunication getLiveviewCommunication()
    {
        return (liveViewControl);
    }

    @Override
    public IFujiXCommunication getAsyncEventCommunication()
    {
        return (asyncReceiver);
    }

    @Override
    public IFujiXCommunication getCommandCommunication()
    {
        return (commandPublisher);
    }

    @Override
    public ICameraStatusWatcher getCameraStatusWatcher()
    {
        return (statusChecker);
    }

    @Override
    public ICameraStatusUpdateNotify getStatusListener()
    {
        return (statusListener);
    }

    @Override
    public ICameraStatus getCameraStatusListHolder()
    {
        return (statusChecker);
    }

    @Override
    public ICameraButtonControl getButtonControl()
    {
        return (fujiXButtonControl);
    }

    @Override
    public IPlaybackControl getPlaybackControl()
    {
        return (playbackControl);
    }

    @Override
    public ICameraHardwareStatus getHardwareStatus()
    {
        return (hardwareStatus);
    }

    @Override
    public ICameraRunMode getCameraRunMode()
    {
        return (runmode);
    }

    @Override
    public void setAsyncEventReceiver(@NonNull IFujiXCommandCallback receiver)
    {
        asyncReceiver.setEventSubscriber(receiver);
    }

}
