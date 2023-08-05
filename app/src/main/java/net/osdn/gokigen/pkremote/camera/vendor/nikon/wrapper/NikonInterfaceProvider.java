package net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.status.NikonStatusChecker;
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
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import java.net.InetAddress;
import java.util.List;

public class NikonInterfaceProvider implements INikonInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();

    private static final int STREAM_PORT = 15742;   // ??
    private static final int ASYNC_RESPONSE_PORT = 15741;  // ??
    private static final int CONTROL_PORT = 15740;
    private static final int EVENT_PORT = 15740;

    private final AppCompatActivity activity;
    private final PtpIpRunMode runMode;
    private final PtpIpHardwareStatus hardwareStatus;
    private final PtpIpButtonControl ptpIpButtonControl;
    private final NikonConnection nikonConnection;
    private final PtpIpCommandPublisher commandPublisher;
    //private final PtpIpCommandPublisher0 commandPublisher;
    private final PtpIpLiveViewControl liveViewControl;
    private final PtpIpAsyncResponseReceiver asyncReceiver;
    private final PtpIpZoomControl zoomControl;
    //private PtpIpCaptureControl captureControl;
    //private PtpIpFocusingControl focusingControl;
    private final NikonStatusChecker statusChecker;
    private final ICameraStatusUpdateNotify statusListener;
    private final NikonPlaybackControl playbackControl;
    private final IInformationReceiver informationReceiver;

    public NikonInterfaceProvider(@NonNull AppCompatActivity context, @NonNull ICameraStatusReceiver provider, @NonNull ICameraStatusUpdateNotify statusListener, @NonNull IInformationReceiver informationReceiver)
    {
        this.activity = context;
        commandPublisher = new PtpIpCommandPublisher(false, false);
        //commandPublisher = new PtpIpCommandPublisher0();
        liveViewControl = new PtpIpLiveViewControl(context, false);
        asyncReceiver = new PtpIpAsyncResponseReceiver();
        statusChecker = new NikonStatusChecker(activity, this);
        nikonConnection = new NikonConnection(context, provider, this, statusChecker);
        zoomControl = new PtpIpZoomControl();
        this.statusListener = statusListener;
        this.runMode = new PtpIpRunMode();
        this.hardwareStatus = new PtpIpHardwareStatus();
        this.ptpIpButtonControl = new PtpIpButtonControl();
        this.playbackControl = new NikonPlaybackControl(activity, this);
        this.informationReceiver = informationReceiver;
    }

    private String getHostAddress(@NonNull AppCompatActivity context)
    {
        String ipAddress = IPreferencePropertyAccessor.NIKON_CAMERA_IP_ADDRESS;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean autoDetactHostIp = preferences.getBoolean(IPreferencePropertyAccessor.NIKON_AUTO_DETECT_HOST_IP, true);
            ipAddress = preferences.getString(IPreferencePropertyAccessor.NIKON_CAMERA_IP_ADDRESS, IPreferencePropertyAccessor.NIKON_CAMERA_IP_ADDRESS);
            if ((autoDetactHostIp)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M))
            {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                Network activeNetwork = connectivityManager.getActiveNetwork();
                if (activeNetwork == null)
                {
                    return (ipAddress);
                }
                LinkProperties linkProperties = connectivityManager.getLinkProperties(activeNetwork);
                if (linkProperties == null)
                {
                    return (ipAddress);
                }
                List<RouteInfo> routes = linkProperties.getRoutes();
                for (RouteInfo route: routes)
                {
                    try
                    {
                        InetAddress gateway = route.getGateway();
                        if ((route.isDefaultRoute())&&(gateway != null))
                        {
                            ipAddress = gateway.toString().replace("/","");
                            Log.v(TAG, " --------- default Gateway : ipAddress  --------- ");
                            break;
                        }
                    }
                    catch (Exception ee)
                    {
                        ee.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (ipAddress);
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
        return (runMode);
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
        return (runMode);
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

    @Override
    public String getIpAddress()
    {
        return getHostAddress(activity);
    }

    @Override
    public int getControlPortNumber()
    {
        return (CONTROL_PORT);
    }

    @Override
    public int getEventPortNumber()
    {
        return (EVENT_PORT);
    }

}
