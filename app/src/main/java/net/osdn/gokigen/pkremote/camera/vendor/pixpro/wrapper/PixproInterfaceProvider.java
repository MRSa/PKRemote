package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Base64;
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
import net.osdn.gokigen.pkremote.camera.utils.SimpleLogDumper;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.IPixproInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommunication;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.PixproCommandCommunicator;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.connection.PixproConnection;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.playback.PixproPlaybackControl;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.status.PixproCameraHardwareStatus;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.status.PixproCameraInformation;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.status.PixproRunMode;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.status.PixproStatusChecker;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT;
import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT_DEFAULT_VALUE;
import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.PIXPRO_HOST_IP;
import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.PIXPRO_HOST_IP_DEFAULT_VALUE;

/**
 *
 *
 */
public class PixproInterfaceProvider implements IPixproInterfaceProvider, IDisplayInjector, IConnectionKeyReceiver, IConnectionKeyProvider
{
    private final String TAG = toString();
    private final Activity activity;
    private final PixproCommandCommunicator commandCommunicator;
    private final IInformationReceiver informationReceiver;
    private final PixproConnection pixproConnection;
    private final ICameraHardwareStatus hardwarestatus;
    private final PixproPlaybackControl playbackControl;
    private final PixproStatusChecker statusChecker;
    private final PixproRunMode runMode;
    private final ICameraInformation cameraInformation;

    private String password = null;
    private byte[] keyphrase = null;

    /**
     *
     *
     */
    public PixproInterfaceProvider(@NonNull Activity activity, @NonNull ICameraStatusReceiver provider, @NonNull IInformationReceiver informationReceiver)
    {
        String ipAddress;
        String controlPortStr;
        int communicationTimeoutMs;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            ipAddress = preferences.getString(PIXPRO_HOST_IP, PIXPRO_HOST_IP_DEFAULT_VALUE);
            controlPortStr = preferences.getString(PIXPRO_COMMAND_PORT, PIXPRO_COMMAND_PORT_DEFAULT_VALUE);
            communicationTimeoutMs = parseInt(preferences.getString(IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT, IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE), 30) * 1000;
            if (communicationTimeoutMs < 3000)
            {
                communicationTimeoutMs = 3000;  // 最小値は 3000msとする。
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ipAddress = "172.16.0.254";
            controlPortStr = "9175";
            communicationTimeoutMs = 30000;  // エラー時は 30000msとする。
        }
        int controlPort = parseInt(controlPortStr, 9175);
        this.commandCommunicator = new PixproCommandCommunicator(this, ipAddress, controlPort, true, false);
        this.informationReceiver = informationReceiver;
        this.activity = activity;
        this.cameraInformation = new PixproCameraInformation();
        this.statusChecker = new PixproStatusChecker();
        this.pixproConnection = new PixproConnection(activity, provider, this, statusChecker);
        this.hardwarestatus = new PixproCameraHardwareStatus();
        this.runMode = new PixproRunMode();
        this.playbackControl = new PixproPlaybackControl(ipAddress, communicationTimeoutMs,this);
    }

    private int parseInt(@NonNull String key, int defaultValue)
    {
        int value = defaultValue;
        try
        {
            value = Integer.parseInt(key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (value);
    }

    public void prepare()
    {
        Log.v(TAG, "prepare()");
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
    }

    @Override
    public ICameraConnection getPixproCameraConnection()
    {
        return (pixproConnection);
    }

    @Override
    public ILiveViewControl getLiveViewControl()
    {
        return (null);
    }

    @Override
    public ILiveViewListener getLiveViewListener()
    {
        return (null);
    }

    @Override
    public IFocusingControl getFocusingControl()
    {
        return (null);
    }

    @Override
    public ICameraInformation getCameraInformation()
    {
        return (cameraInformation);
    }

    @Override
    public IZoomLensControl getZoomLensControl()
    {
        return (null);
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
    public ICameraStatus getCameraStatusListHolder()
    {
        return (statusChecker);
    }

    @Override
    public ICameraButtonControl getButtonControl()
    {
        return (null);
    }

    @Override
    public ICameraStatusWatcher getCameraStatusWatcher()
    {
        return (statusChecker);
    }

    @Override
    public IPlaybackControl getPlaybackControl()
    {
        return (playbackControl);
    }

    @Override
    public ICameraHardwareStatus getHardwareStatus()
    {
        return (hardwarestatus);
    }

    @Override
    public ICameraRunMode getCameraRunMode()
    {
        return (runMode);
    }

    @Override
    public IPixproCommandPublisher getCommandPublisher()
    {
        return (commandCommunicator);
    }

    @Override
    public IPixproCommunication getCommandCommunication()
    {
        return (commandCommunicator);
    }

    @Override
    public IConnectionKeyReceiver getConnectionKeyReceiver()
    {
        return (this);
    }

    @Override
    public IInformationReceiver getInformationReceiver()
    {
        return (informationReceiver);
    }

    @Override
    public String getStringFromResource(int resId)
    {
        return (activity.getString(resId));
    }

    @Override
    public void receivedPassword(@NonNull String password)
    {
        Log.v(TAG, " receivedPassword [" + password.length() + "] : " + password);
        this.password = password;
    }

    @Override
    public void receivedKeyString(@NonNull byte[] keyString)
    {
        Log.v(TAG, " receivedKeyString");
        SimpleLogDumper.dump_bytes(" Key[" + keyString.length + "]", keyString);
        this.keyphrase = keyString;
    }

    @Override
    public String getUserString()
    {
        return ("usr=dscuser");
    }

    @Override
    public String getPasswordString()
    {
        String passwordString = "pwd=";
        try
        {
            if (password != null)
            {
                passwordString = passwordString + password + "&";
            }
            if (keyphrase != null)
            {
                // Base64 変換  22文字で切って埋める。
                String encodeString = Base64.encodeToString(keyphrase, Base64.DEFAULT);
                passwordString = passwordString + encodeString.substring(0, 22) + "==";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            passwordString = "pwd=12345678";
        }
        return (passwordString);
    }
}
