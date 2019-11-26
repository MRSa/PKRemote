package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.playback;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.util.Date;

public class PanasonicImageContentInfo implements ICameraContent
{
    private final String TAG = toString();
    private String targetUrl;
    private Date date;
    private boolean isDateValid = false;

    PanasonicImageContentInfo(String targetUrl)
    {
        this.targetUrl = targetUrl;
        this.date = new Date();
    }

    @Override
    public String getCameraId()
    {
        return "";
    }

    @Override
    public String getCardId()
    {
        return "";
    }

    @Override
    public String getContentPath()
    {
        return "";
    }

    @Override
    public String getContentName()
    {
        return (targetUrl.substring(targetUrl.lastIndexOf("/") + 1));
    }

    @Override
    public String getOriginalName()
    {
        return (getContentName());
    }

    @Override
    public boolean isRaw()
    {
        try
        {
            String target = getContentName().toLowerCase();
            return ((target.endsWith("rw2")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    @Override
    public boolean isMovie()
    {
        try
        {
            String target = getContentName().toLowerCase();
            return ((target.endsWith("mov")) || (target.endsWith("mp4")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    @Override
    public boolean isDateValid() {
        return (isDateValid);
    }

    @Override
    public boolean isContentNameValid()
    {
        return (true);
    }

    @Override
    public Date getCapturedDate()
    {
        return (date);
    }

    @Override
    public void setCapturedDate(Date date)
    {
        Log.v(TAG, "setCapturedDate()");
        this.date = date;
        isDateValid = true;
    }
}
