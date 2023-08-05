package net.osdn.gokigen.pkremote.camera.vendor.nikon;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.IInformationReceiver;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IDisplayInjector;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraHardwareStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraInformation;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status.IPtpIpRunModeHolder;

public interface INikonInterfaceProvider
{
    ICameraConnection getPtpIpCameraConnection();
    ILiveViewControl getLiveViewControl();
    ILiveViewListener getLiveViewListener();
    IFocusingControl getFocusingControl();
    ICameraInformation getCameraInformation();
    IZoomLensControl getZoomLensControl();
    ICaptureControl getCaptureControl();
    IDisplayInjector getDisplayInjector();

    IPtpIpRunModeHolder getRunModeHolder();
    IPtpIpCommandCallback getStatusHolder();
    IPtpIpCommandPublisher getCommandPublisher();
    IPtpIpCommunication getLiveviewCommunication();
    IPtpIpCommunication getAsyncEventCommunication();
    IPtpIpCommunication getCommandCommunication();

    ICameraStatusUpdateNotify getStatusListener();

    ICameraStatusWatcher getCameraStatusWatcher();
    ICameraStatus getCameraStatusListHolder();

    ICameraButtonControl getButtonControl();
    IPlaybackControl getPlaybackControl();
    ICameraHardwareStatus getHardwareStatus();
    ICameraRunMode getCameraRunMode();

    IInformationReceiver getInformationReceiver();

    void setAsyncEventReceiver(@NonNull IPtpIpCommandCallback receiver);

    String getIpAddress();
    int getControlPortNumber();
    int getEventPortNumber();
}
