package net.osdn.gokigen.pkremote.camera.interfaces.control;

public interface ICameraRunMode
{
    /** カメラの動作モード変更 **/
    void changeRunMode(boolean isRecording);
    boolean isRecordingMode();
}
