package net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper.playback;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class OlympusPenCameraContent implements ICameraContent
{
    private final String TAG = toString();

    private final String path;
    private final String name;
    private final String size;
    private final String attribute;
    private final String date;
    private final String time;

    OlympusPenCameraContent(String path, String name, String size, String attribute, String date, String time)
    {
        Log.v(TAG, "  " + path + " " + name + "  size: " + size + " ");

        this.path = path;
        this.name = name;
        this.size = size;
        this.attribute = attribute;
        this.date = date;
        this.time = time;
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
        return (path);
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
        try
        {
            String target = name.toLowerCase();
            return ((target.endsWith("orf")));
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
            String target = name.toLowerCase();
            return ((target.endsWith("mov")) || (target.endsWith("mp4")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    @Override
    public boolean isDateValid()
    {
        return (true);
    }

    @Override
    public boolean isContentNameValid()
    {
        return (true);
    }

    @Override
    public Date getCapturedDate()
    {
        try
        {
            int timeInt = Integer.parseInt(time);
            int dateInt = Integer.parseInt(date);

            int year = ((dateInt & 0xfe00) >> 9) + 1980;
            int month = ((dateInt & 0x01f0) >> 5);
            int day =  (dateInt & 0x001f);

            int sec = ((timeInt & 0x001f) << 1); // 2倍...
            int min = ((timeInt & 0x07e0) >> 5);
            int hour = ((timeInt & 0x07e0) >> 11);

            //Log.v(TAG, " " + year + "/" + month + "/" + day + "  " + (hour + 1) + ":" + min + ":" + sec);

            Calendar cal = new GregorianCalendar();
            cal.set(year, (month - 1), day, hour, min, sec);
            return (cal.getTime());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new Date());
    }

    @Override
    public void setCapturedDate(Date date)
    {

    }
}
