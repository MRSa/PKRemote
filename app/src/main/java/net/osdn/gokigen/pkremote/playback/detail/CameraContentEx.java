package net.osdn.gokigen.pkremote.playback.detail;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.util.Locale;

public class CameraContentEx
{
    private final ICameraContent fileInfo;
    private static final String MOVIE_SUFFIX = ".mov";
    private static final String MOVIE_SUFFIX_MP4 = ".mp4";

    private String rawSuffix;
    private boolean hasRaw;
    private boolean isSelected;

    public CameraContentEx(ICameraContent fileInfo, boolean hasRaw, String rawSuffix)
    {
        this.fileInfo = fileInfo;
        this.hasRaw = hasRaw;
        this.rawSuffix = rawSuffix;
        this.isSelected = false;
    }

    public void setHasRaw(boolean value, String rawSuffix)
    {
        hasRaw = value;
        this.rawSuffix = rawSuffix;
    }

    public void setSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }

    public boolean hasRaw()
    {
        if (!(fileInfo.isContentNameValid()))
        {
            //


        }
        return (hasRaw);
    }

    public boolean isMovie()
    {
        String contentName = fileInfo.getContentName().toLowerCase(Locale.ENGLISH);
        return ((contentName.endsWith(MOVIE_SUFFIX))||(contentName.endsWith(MOVIE_SUFFIX_MP4)));
    }

    public boolean isSelected()
    {
        return (isSelected);
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
