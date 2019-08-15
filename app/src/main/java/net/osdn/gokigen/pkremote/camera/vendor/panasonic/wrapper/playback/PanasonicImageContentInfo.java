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
    public boolean isDateValid() {
        return (isDateValid);
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
