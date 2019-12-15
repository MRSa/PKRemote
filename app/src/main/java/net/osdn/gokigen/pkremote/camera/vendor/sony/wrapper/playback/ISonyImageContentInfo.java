package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.playback;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

interface ISonyImageContentInfo extends ICameraContent
{
    String getOriginalUrl();
    String getLargeUrl();
    String getSmallUrl();
    String getThumbnailUrl();
}
