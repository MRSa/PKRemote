package net.osdn.gokigen.pkremote.camera.interfaces;

import net.osdn.gokigen.pkremote.IInformationReceiver;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IDisplayInjector;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentsRecognizer;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraHardwareStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraInformation;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher;
import net.osdn.gokigen.pkremote.camera.vendor.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.sony.ISonyInterfaceProvider;

/**
 *
 */
public interface IInterfaceProvider
{
    ICameraConnection getCameraConnection();
    ICameraButtonControl getButtonControl();

    IDisplayInjector getDisplayInjector();

    ILiveViewControl getLiveViewControl();
    ILiveViewListener getLiveViewListener();
    IFocusingControl getFocusingControl();
    ICameraInformation getCameraInformation();
    IZoomLensControl getZoomLensControl();
    ICaptureControl getCaptureControl();
    ICameraStatus getCameraStatusListHolder();
    ICameraStatusWatcher getCameraStatusWatcher();

    IPlaybackControl getPlaybackControl();

    ICameraHardwareStatus getHardwareStatus();
    ICameraRunMode getCameraRunMode();

    IOlympusInterfaceProvider getOlympusInterfaceProvider();
    ISonyInterfaceProvider getSonyInterface();

    ICameraConnection.CameraConnectionMethod getCammeraConnectionMethod();
    void resetCameraConnectionMethod();

    IInformationReceiver getInformationReceiver();
    ICameraContentsRecognizer getCameraContentsRecognizer();
}