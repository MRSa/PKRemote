package net.osdn.gokigen.pkremote.camera.interfaces.status;

/**
 *
 *
 */
public interface ICameraStatusReceiver
{
    void onStatusNotify(String message);
    void onCameraConnected();
    void onCameraDisconnected();
    void onCameraOccursException(String message, Exception e);
}
