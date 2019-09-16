package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status;

public interface IPtpIpRunModeHolder
{
    void transitToRecordingMode(boolean isFinished);
    void transitToPlaybackMode(boolean isFinished);
}
