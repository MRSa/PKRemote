package net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;

import android.util.Log;

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
import net.osdn.gokigen.pkremote.camera.vendor.olympuspen.IOlympusPenInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper.connection.OlympusPenConnection;
import net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper.hardware.OlympusPenButtonControl;
import net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper.hardware.OlympusPenHardwareStatus;
import net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper.playback.OlympusPenPlaybackControl;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 *
 *
 */
public class OlympusPenInterfaceProvider implements IOlympusPenInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();
    //private final Activity activity;
    //private final ICameraStatusReceiver provider;
    private final OlympusPenConnection olympusPenConnection;
    private final OlympusPenButtonControl buttonControl;
    private final OlympusPenPlaybackControl playbackControl;
    private final OlympusPenHardwareStatus hardwareStatus;
    private final OlympusPenRunMode runMode;

    /**
     *
     *
     */
    public OlympusPenInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //useGrCommand = preferences.getBoolean(IPreferencePropertyAccessor.USE_GR2_SPECIAL_COMMAND, true);
        //pentaxCaptureAfterAf = preferences.getBoolean(IPreferencePropertyAccessor.PENTAX_CAPTURE_AFTER_AF, false);
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
        int maxCount = 3000;  // デフォルトは 3000枚 とする
        try
        {
            maxCount = Integer.parseInt(preferences.getString(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_MAX_COUNT, IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_MAX_COUNT_DEFAULT_VALUE));
            if (maxCount < 300)
            {
                maxCount = 300;  // 最小値は 300枚とする。
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //this.activity = context;
        //this.provider = provider;
        olympusPenConnection = new OlympusPenConnection(context, provider);
        //liveViewControl = new RicohGr2LiveViewControl(useGrCommand);
        //zoomControl = new RicohGr2CameraZoomLensControl();
        buttonControl = new OlympusPenButtonControl();
        //statusChecker = new RicohGr2StatusChecker(500, useGrCommand);
        playbackControl = new OlympusPenPlaybackControl(communicationTimeoutMs);
        hardwareStatus = new OlympusPenHardwareStatus();
        runMode = new OlympusPenRunMode();
    }

    public void prepare()
    {
        Log.v(TAG, "prepare()");
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        //focusControl = new RicohGr2CameraFocusControl(useGrCommand, frameDisplayer, indicator);
        //captureControl = new RicohGr2CameraCaptureControl(useGrCommand, pentaxCaptureAfterAf, frameDisplayer, statusChecker);
    }

    @Override
    public ICameraConnection getOlyCameraConnection()
    {
        return (olympusPenConnection);
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
    public IDisplayInjector getDisplayInjector() {
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
    public ICameraStatusWatcher getCameraStatusWatcher() {
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
