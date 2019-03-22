package net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper;


import android.app.Activity;

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
import net.osdn.gokigen.pkremote.camera.vendor.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper.connection.OlyCameraConnection;
import net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper.playback.OlyCameraPlaybackControl;
import net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper.property.IOlyCameraPropertyProvider;
import net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper.property.OlyCameraPropertyProxy;

/**
 *
 *
 */
public class OlympusInterfaceProvider implements IOlympusInterfaceProvider, IDisplayInjector
{
    private final OlyCameraWrapper wrapper;
    private final OlyCameraConnection connection;
    private final OlyCameraPropertyProxy propertyProxy;
    private final OlyCameraHardwareStatus hardwareStatus;
    private final OLYCameraPropertyListenerImpl propertyListener;
    private final OlyCameraZoomLensControl zoomLensControl;
    private final OlyCameraPlaybackControl playbackControl;
    private final OlyCameraStatusWrapper statusWrapper;
    private OlyCameraFocusControl focusControl = null;
    private OlyCameraCaptureControl captureControl = null;


    public OlympusInterfaceProvider(Activity context, ICameraStatusReceiver provider)
    {
        this.wrapper = new OlyCameraWrapper(context);
        this.connection = new OlyCameraConnection(context, this.wrapper.getOLYCamera(), provider);
        this.propertyProxy = new OlyCameraPropertyProxy(this.wrapper.getOLYCamera());
        this.hardwareStatus = new OlyCameraHardwareStatus(this.wrapper.getOLYCamera());
        this.propertyListener = new OLYCameraPropertyListenerImpl(this.wrapper.getOLYCamera());
        this.zoomLensControl = new OlyCameraZoomLensControl(context, this.wrapper.getOLYCamera());
        this.playbackControl = new OlyCameraPlaybackControl(this.wrapper.getOLYCamera(), context);
        this.statusWrapper = new OlyCameraStatusWrapper(this.wrapper.getOLYCamera());
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        focusControl = new OlyCameraFocusControl(wrapper, frameDisplayer, indicator);
        captureControl = new OlyCameraCaptureControl (wrapper, frameDisplayer, indicator);
        propertyListener.setFocusingControl(focusingModeNotify);
    }

    @Override
    public ICameraConnection getOlyCameraConnection()
    {
        return (connection);
    }

    @Override
    public ICameraHardwareStatus getHardwareStatus()
    {
        return (hardwareStatus);
    }

    @Override
    public IOlyCameraPropertyProvider getCameraPropertyProvider()
    {
        return (propertyProxy);
    }

    @Override
    public ICameraStatus getCameraStatusListHolder()
    {
        return (statusWrapper);
    }

    @Override
    public ICameraStatusWatcher getCameraStatusWatcher()
    {
        return (statusWrapper);
    }

    @Override
    public IPlaybackControl getPlaybackControl()
    {
        return (playbackControl);
    }

    @Override
    public ICameraRunMode getCameraRunMode()
    {
        return (wrapper);
    }

    @Override
    public IZoomLensControl getZoomLensControl()
    {
        return (zoomLensControl);
    }

    @Override
    public ILiveViewControl getLiveViewControl()
    {
        return (wrapper);
    }

    @Override
    public ILiveViewListener getLiveViewListener()
    {
        return (wrapper);
    }

    @Override
    public IFocusingControl getFocusingControl()
    {
        return (focusControl);
    }

    @Override
    public ICaptureControl getCaptureControl() {
        return (captureControl);
    }

    @Override
    public ICameraButtonControl getButtonControl()
    {
        return (null);
    }

    @Override
    public ICameraInformation getCameraInformation()
    {
        return (propertyListener);
    }

    @Override
    public IDisplayInjector getDisplayInjector()
    {
        return (this);
    }
}
