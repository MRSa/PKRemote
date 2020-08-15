package net.osdn.gokigen.pkremote.camera.vendor.pixpro;

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
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraHardwareStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraInformation;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.IConnectionKeyReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommunication;

public interface IPixproInterfaceProvider
{
    ICameraConnection getPixproCameraConnection();
    ILiveViewControl getLiveViewControl();

    ILiveViewListener getLiveViewListener();
    IFocusingControl getFocusingControl();
    ICameraInformation getCameraInformation();
    IZoomLensControl getZoomLensControl();
    ICaptureControl getCaptureControl();
    IDisplayInjector getDisplayInjector();

    ICameraStatusWatcher getCameraStatusWatcher();
    ICameraStatus getCameraStatusListHolder();

    ICameraButtonControl getButtonControl();

    IPlaybackControl getPlaybackControl();
    ICameraHardwareStatus getHardwareStatus();
    ICameraRunMode getCameraRunMode();

    IPixproCommandPublisher getCommandPublisher();
    IPixproCommunication getCommandCommunication();

    IConnectionKeyReceiver getConnectionKeyReceiver();

    IInformationReceiver getInformationReceiver();
    String getStringFromResource(int resId);
}
