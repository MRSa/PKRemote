package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.status;

public interface IFujiXRunModeHolder
{
    void transitToRecordingMode(boolean isFinished);
    void transitToPlaybackMode(boolean isFinished);
}
