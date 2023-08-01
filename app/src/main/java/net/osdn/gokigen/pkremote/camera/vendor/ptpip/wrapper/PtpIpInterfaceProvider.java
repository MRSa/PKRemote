package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;
import android.os.Build;
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
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.operation.PtpIpZoomControl;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.PtpIpAsyncResponseReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.PtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.connection.CanonConnection;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.liveview.PtpIpLiveViewControl;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback.CanonPlaybackControl;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status.IPtpIpRunModeHolder;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status.PtpIpStatusChecker;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import java.net.InetAddress;
import java.util.List;

public class PtpIpInterfaceProvider implements IPtpIpInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();

    private static final int STREAM_PORT = 15742;   // ??
    private static final int ASYNC_RESPONSE_PORT = 15741;  // ??
    private static final int CONTROL_PORT = 15740;
    private static final int EVENT_PORT = 15740;

    //private static final String CAMERA_IP = "192.168.0.1";
    //private final Activity activity;

    private final PtpIpRunMode runmode;
    private final PtpIpHardwareStatus hardwareStatus;
    private final PtpIpButtonControl ptpIpButtonControl;
    private final CanonConnection canonConnection;
    //private final PtpIpCommandPublisher0 commandPublisher;
    private final PtpIpCommandPublisher commandPublisher;
    private final PtpIpLiveViewControl liveViewControl;
    private final PtpIpAsyncResponseReceiver asyncReceiver;
    private final PtpIpZoomControl zoomControl;
    //private PtpIpCaptureControl captureControl;
    //private PtpIpFocusingControl focusingControl;
    private final PtpIpStatusChecker statusChecker;
    private final ICameraStatusUpdateNotify statusListener;
    private final CanonPlaybackControl playbackControl;
    private final IInformationReceiver informationReceiver;

    public PtpIpInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider, @NonNull ICameraStatusUpdateNotify statusListener, @NonNull IInformationReceiver informationReceiver)
    {
        //this.activity = context;
        String ipAddress; // "192.168.0.1";
        int sequenceType = 0;
        try
        {
            ipAddress = getHostAddress(context);
            try
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                String sequenceTypeStr = preferences.getString(IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE, IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE_DEFAULT_VALUE);
                sequenceType = Integer.parseInt(sequenceTypeStr);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ipAddress = "192.168.0.1";
        }

        commandPublisher = new PtpIpCommandPublisher(ipAddress, CONTROL_PORT, false, false);
        //commandPublisher = new PtpIpCommandPublisher0(ipAddress, CONTROL_PORT);
        liveViewControl = new PtpIpLiveViewControl(context, ipAddress, STREAM_PORT);
        asyncReceiver = new PtpIpAsyncResponseReceiver(ipAddress, ASYNC_RESPONSE_PORT);
        statusChecker = new PtpIpStatusChecker(context, commandPublisher, ipAddress, EVENT_PORT);
        canonConnection = new CanonConnection(context, provider, this, statusChecker, ipAddress, sequenceType);
        zoomControl = new PtpIpZoomControl();
        this.statusListener = statusListener;
        this.runmode = new PtpIpRunMode();
        this.hardwareStatus = new PtpIpHardwareStatus();
        this.ptpIpButtonControl = new PtpIpButtonControl();
        this.playbackControl = new CanonPlaybackControl(context, this);
        this.informationReceiver = informationReceiver;
    }

    private String getHostAddress(@NonNull Activity context)
    {
        String ipAddress = "192.168.0.1";
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean autoDetactHostIp = preferences.getBoolean(IPreferencePropertyAccessor.CANON_AUTO_DETECT_HOST_IP, true);
            ipAddress = preferences.getString(IPreferencePropertyAccessor.CANON_HOST_IP, IPreferencePropertyAccessor.CANON_HOST_IP_DEFAULT_VALUE);
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
        return (canonConnection);
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
