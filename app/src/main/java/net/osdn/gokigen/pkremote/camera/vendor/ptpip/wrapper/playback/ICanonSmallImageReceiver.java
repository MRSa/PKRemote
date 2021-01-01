package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;

public interface ICanonSmallImageReceiver
{
    void issueCommand(final int objectId, IDownloadContentCallback callback);
}
