package net.osdn.gokigen.pkremote.camera.vendor.ricoh.wrapper;

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
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.IRicohGr2InterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation.RicohGr2CameraButtonControl;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation.RicohGr2CameraCaptureControl;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation.RicohGr2CameraFocusControl;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation.RicohGr2CameraZoomLensControl;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation.RicohGr2HardwareStatus;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.wrapper.connection.RicohGr2Connection;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 *
 *
 */
public class RicohGr2InterfaceProvider implements IRicohGr2InterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();
    //private final Activity activity;
    //private final ICameraStatusReceiver provider;
    private final RicohGr2Connection gr2Connection;
    private final RicohGr2CameraButtonControl buttonControl;
    private final RicohGr2StatusChecker statusChecker;
    private final RicohGr2PlaybackControl playbackControl;
    private final RicohGr2HardwareStatus hardwareStatus;
    private final RicohGr2RunMode runMode;
    private final boolean useGrCommand;
    private final boolean pentaxCaptureAfterAf;

    private RicohGr2LiveViewControl liveViewControl;
    private RicohGr2CameraCaptureControl captureControl;
    private RicohGr2CameraZoomLensControl zoomControl;
    private RicohGr2CameraFocusControl focusControl;

    /**
     *
     *
     */
    public RicohGr2InterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        useGrCommand = preferences.getBoolean(IPreferencePropertyAccessor.USE_GR2_SPECIAL_COMMAND, true);
        pentaxCaptureAfterAf = preferences.getBoolean(IPreferencePropertyAccessor.PENTAX_CAPTURE_AFTER_AF, false);

        //this.activity = context;
        //this.provider = provider;
        gr2Connection = new RicohGr2Connection(context, provider);
        liveViewControl = new RicohGr2LiveViewControl(useGrCommand);
        zoomControl = new RicohGr2CameraZoomLensControl();
        buttonControl = new RicohGr2CameraButtonControl();
        statusChecker = new RicohGr2StatusChecker(500, useGrCommand);
        playbackControl = new RicohGr2PlaybackControl();
        hardwareStatus = new RicohGr2HardwareStatus();
        runMode = new RicohGr2RunMode();
    }

    public void prepare()
    {
        Log.v(TAG, "prepare()");
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        focusControl = new RicohGr2CameraFocusControl(useGrCommand, frameDisplayer, indicator);
        captureControl = new RicohGr2CameraCaptureControl(useGrCommand, pentaxCaptureAfterAf, frameDisplayer, statusChecker);
    }

    @Override
    public ICameraConnection getRicohGr2CameraConnection()
    {
        return (gr2Connection);
    }

    @Override
    public ILiveViewControl getLiveViewControl()
    {
        return (liveViewControl);
    }

    @Override
    public ILiveViewListener getLiveViewListener()
    {
        if (liveViewControl == null)
        {
            return (null);
        }
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
    public IDisplayInjector getDisplayInjector() {
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
        return (buttonControl);
    }

    @Override
    public ICameraStatusWatcher getCameraStatusWatcher() {
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
        return (hardwareStatus);
    }

    @Override
    public ICameraRunMode getCameraRunMode()
    {
        return (runMode);
    }
}
