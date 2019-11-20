package net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.IInformationReceiver;
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
import net.osdn.gokigen.pkremote.camera.vendor.nikon.INikonInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.playback.NikonPlaybackControl;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.operation.PtpIpZoomControl;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.PtpIpButtonControl;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.PtpIpHardwareStatus;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.PtpIpRunMode;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.PtpIpAsyncResponseReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.PtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.connection.NikonConnection;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.liveview.PtpIpLiveViewControl;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status.IPtpIpRunModeHolder;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status.PtpIpStatusChecker;

import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.NIKON_CAMERA_IP_ADDRESS;
import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.NIKON_CAMERA_IP_ADDRESS_DEFAULT_VALUE;

public class NikonInterfaceProvider implements INikonInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();

    private static final int STREAM_PORT = 15742;   // ??
    private static final int ASYNC_RESPONSE_PORT = 15741;  // ??
    private static final int CONTROL_PORT = 15740;
    private static final int EVENT_PORT = 15740;
    private static final String DEFAULT_CAMERA_IP_ADDR = "192.168.1.1";

    private final Activity activity;
    private final PtpIpRunMode runmode;
    private final PtpIpHardwareStatus hardwareStatus;
    private PtpIpButtonControl ptpIpButtonControl;
    private NikonConnection nikonConnection;
    private PtpIpCommandPublisher commandPublisher;
    private PtpIpLiveViewControl liveViewControl;
    private PtpIpAsyncResponseReceiver asyncReceiver;
    private PtpIpZoomControl zoomControl;
    //private PtpIpCaptureControl captureControl;
    //private PtpIpFocusingControl focusingControl;
    private PtpIpStatusChecker statusChecker;
    private ICameraStatusUpdateNotify statusListener;
    private NikonPlaybackControl playbackControl;
    private IInformationReceiver informationReceiver;

    public NikonInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider, @NonNull ICameraStatusUpdateNotify statusListener, @NonNull IInformationReceiver informationReceiver)
    {
        this.activity = context;
        String ipAddress = DEFAULT_CAMERA_IP_ADDR;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            ipAddress = preferences.getString(NIKON_CAMERA_IP_ADDRESS, NIKON_CAMERA_IP_ADDRESS_DEFAULT_VALUE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        commandPublisher = new PtpIpCommandPublisher(ipAddress, CONTROL_PORT);
        liveViewControl = new PtpIpLiveViewControl(context, ipAddress, STREAM_PORT);
        asyncReceiver = new PtpIpAsyncResponseReceiver(ipAddress, ASYNC_RESPONSE_PORT);
        statusChecker = new PtpIpStatusChecker(activity, commandPublisher, ipAddress, EVENT_PORT);
        nikonConnection = new NikonConnection(context, provider, this, statusChecker);
        zoomControl = new PtpIpZoomControl();
        this.statusListener = statusListener;
        this.runmode = new PtpIpRunMode();
        this.hardwareStatus = new PtpIpHardwareStatus();
        this.ptpIpButtonControl = new PtpIpButtonControl();
        this.playbackControl = new NikonPlaybackControl(activity, this);
        this.informationReceiver = informationReceiver;
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        //captureControl = new FujiXCaptureControl(commandPublisher, frameDisplayer);
        //focusingControl = new FujiXFocusingControl(activity, commandPublisher, frameDisplayer, indicator);
    }

    @Override
    public ICameraConnection getPtpIpCameraConnection()
    {
        return (nikonConnection);
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
        return (null);
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
        return (null);
    }

    @Override
    public IDisplayInjector getDisplayInjector()
    {
        return (this);
    }

    @Override
    public IPtpIpRunModeHolder getRunModeHolder()
    {
        return (runmode);
    }

    @Override
    public IPtpIpCommandCallback getStatusHolder() {
        return (statusChecker);
    }

    @Override
    public IPtpIpCommandPublisher getCommandPublisher()
    {
        return (commandPublisher);
    }

    @Override
    public IPtpIpCommunication getLiveviewCommunication()
    {
        return (liveViewControl);
    }

    @Override
    public IPtpIpCommunication getAsyncEventCommunication()
    {
        return (asyncReceiver);
    }

    @Override
    public IPtpIpCommunication getCommandCommunication()
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
        return (ptpIpButtonControl);
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
    public IInformationReceiver getInformationReceiver()
    {
        // ちょっとこの引き回しは気持ちがよくない...
        return (informationReceiver);
    }

    @Override
    public void setAsyncEventReceiver(@NonNull IPtpIpCommandCallback receiver)
    {
        asyncReceiver.setEventSubscriber(receiver);
    }

}
