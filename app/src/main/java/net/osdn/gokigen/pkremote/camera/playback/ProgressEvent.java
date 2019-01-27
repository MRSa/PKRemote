package net.osdn.gokigen.pkremote.camera.playback;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.IProgressEvent;

import androidx.annotation.Nullable;

public class ProgressEvent implements IProgressEvent
{
    private final float percent;
    private final CancelCallback callback;

    public ProgressEvent(float percent, @Nullable CancelCallback callback)
    {
        this.percent = percent;
        this.callback = callback;
    }

    public float getProgress()
    {
        return (percent);
    }

    public boolean isCancellable()
    {
        return ((callback != null));
    }

    public void requestCancellation()
    {
        if (callback != null)
        {
            callback.requestCancellation();
        }
    }
}
