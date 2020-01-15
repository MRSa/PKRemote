package net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

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
import net.osdn.gokigen.pkremote.camera.vendor.theta.IThetaInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.connection.ThetaConnection;
import net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.hardware.ThetaButtonControl;
import net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.hardware.ThetaHardwareStatus;
import net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.playback.ThetaPlaybackControl;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

/**
 *
 *
 */
public class ThetaInterfaceProvider implements IThetaInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();
    private final ThetaConnection thetaConnection;
    private final ThetaButtonControl buttonControl;
    private final ThetaPlaybackControl playbackControl;
    private final ThetaHardwareStatus hardwareStatus;
    private final ThetaRunMode runMode;

    /**
     *
     *
     */
    public ThetaInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int communicationTimeoutMs = 10000;  // デフォルトは 10000ms とする
        try
        {
            communicationTimeoutMs = Integer.parseInt(preferences.getString(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT, IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE)) * 1000;
            if (communicationTimeoutMs < 3000)
            {
                communicationTimeoutMs = 3000;  // 最小値は 3000msとする。
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //this.activity = context;
        //this.provider = provider;
        thetaConnection = new ThetaConnection(context, provider);
        buttonControl = new ThetaButtonControl();
        playbackControl = new ThetaPlaybackControl(context, communicationTimeoutMs);
        hardwareStatus = new ThetaHardwareStatus();
        runMode = new ThetaRunMode();
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
    public ICameraConnection getThetaCameraConnection()
    {
        return (thetaConnection);
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
        return (null);
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
        return (null);
    }

    @Override
    public ICameraButtonControl getButtonControl()
    {
        return (buttonControl);
    }

    @Override
    public ICameraStatusWatcher getCameraStatusWatcher()
    {
        return (null);
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

}
