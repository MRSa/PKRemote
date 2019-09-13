package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.playback;

class ContentDirectoryInfo
{
    private final String objectId;
    private final int count;

    ContentDirectoryInfo(String objectId, int count)
    {
        this.objectId = objectId;
        this.count = count;
    }

    String getObjectId()
    {
        return (objectId);
    }

    int getCount()
    {
        return (count);
    }
}
