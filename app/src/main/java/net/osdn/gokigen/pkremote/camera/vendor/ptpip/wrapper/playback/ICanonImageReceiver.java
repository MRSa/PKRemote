package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;

public interface ICanonImageReceiver
{
    void issueCommand(final int objectId, int imageSize, IDownloadContentCallback callback);
}
