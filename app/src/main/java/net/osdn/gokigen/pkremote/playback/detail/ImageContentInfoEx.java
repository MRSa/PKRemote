package net.osdn.gokigen.pkremote.playback.detail;


import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;

public class ImageContentInfoEx
{
    private final ICameraFileInfo fileInfo;
    private String rawSuffix;
    private boolean hasRaw;

    public ImageContentInfoEx(ICameraFileInfo fileInfo, boolean hasRaw, String rawSuffix)
    {
        this.fileInfo = fileInfo;
        this.hasRaw = hasRaw;
        this.rawSuffix = rawSuffix;
    }

    public void setHasRaw(boolean value, String rawSuffix)
    {
        hasRaw = value;
        this.rawSuffix = rawSuffix;
    }

    boolean hasRaw()
    {
        return (hasRaw);
    }

    String getRawSuffix()
    {
        return (rawSuffix);
    }

    public ICameraFileInfo getFileInfo()
    {
        return (fileInfo);
    }
}
