package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.playback;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.util.Date;

public class PixproCameraContent implements ICameraContent
{
    private final String pathRoot;
    private String name;
    private String fPath;
    private String size;
    private String dateTime;
    private String dcfIndex;
    private String attr;

    PixproCameraContent(String pathRoot)
    {
        this.pathRoot = pathRoot;

    }

    public void setObjName(String name)
    {
        this.name = name;
    }

    public void setFilePath(String fPath)
    {
        this.fPath = fPath;
    }

    public void setObjSize(String size)
    {
        this.size = size;
    }

    public void setDateTime(String dateTime)
    {
        this.dateTime = dateTime;
    }

    public void setDcfIndex(String dcfIndex)
    {
        this.dcfIndex = dcfIndex;
    }

    public void setAttr(String attr)
    {
        this.attr = attr;
    }

    @Override
    public String getCameraId()
    {
        return ("");
    }

    @Override
    public String getCardId()
    {
        return ("");
    }

    @Override
    public String getContentPath()
    {
        try
        {
            int index = fPath.lastIndexOf("/");
            return (fPath.substring(0, index));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (fPath);
    }

    @Override
    public String getContentName()
    {
        return (name);
    }

    @Override
    public String getOriginalName()
    {
        return (name);
    }

    @Override
    public boolean isRaw()
    {
        return (false);
    }

    @Override
    public boolean isMovie()
    {
        return (false);
    }

    @Override
    public boolean isDateValid()
    {
        return (false);
    }

    @Override
    public boolean isContentNameValid()
    {
        return (false);
    }

    @Override
    public Date getCapturedDate()
    {
        // ‎2020‎年‎7‎月‎11‎日、 ‏‎8:32:30  が 1357595663 (100_0001.JPG)
        // ‎2020‎年‎7‎月‎11‎日、 ‏‎8:33:44  が 1357595702 (100_0002.JPG)
        // ‎2020‎年‎7‎月‎11‎日、 ‏‎8:34:34  が 1357595729 (100_0003.JPG)
        // ‎2020‎年‎8‎月‎10‎日、‏‎20:43:32  が 1359652208

        return (new Date());
    }

    @Override
    public void setCapturedDate(Date date)
    {

    }
}
