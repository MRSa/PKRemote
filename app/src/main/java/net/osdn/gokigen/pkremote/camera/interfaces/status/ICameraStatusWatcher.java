package net.osdn.gokigen.pkremote.camera.interfaces.status;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraStatusUpdateNotify;

public interface ICameraStatusWatcher
{
    void startStatusWatch(ICameraStatusUpdateNotify notifier);
    void stopStatusWatch();
}
