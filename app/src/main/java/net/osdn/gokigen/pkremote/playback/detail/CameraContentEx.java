package net.osdn.gokigen.pkremote.playback.detail;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.util.Locale;

public class CameraContentEx
{
    private final ICameraContent fileInfo;
    private static final String MOVIE_SUFFIX = ".mov";
    private String rawSuffix;
    private boolean hasRaw;

    public CameraContentEx(ICameraContent fileInfo, boolean hasRaw, String rawSuffix)
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

    public boolean hasRaw()
    {
        return (hasRaw);
    }

    public boolean isMovie()
    {
        String contentName = fileInfo.getContentName().toLowerCase(Locale.ENGLISH);
        return (contentName.endsWith(MOVIE_SUFFIX));
    }

    public String getRawSuffix()
    {
        return (rawSuffix);
    }

    public ICameraContent getFileInfo()
    {
        return (fileInfo);
    }

}
