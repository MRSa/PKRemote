package net.osdn.gokigen.pkremote.camera.interfaces.playback;

public interface IProgressEvent
{
    float getProgress();
    boolean isCancellable();
    void requestCancellation();

    interface CancelCallback
    {
        void requestCancellation();
    }
}
