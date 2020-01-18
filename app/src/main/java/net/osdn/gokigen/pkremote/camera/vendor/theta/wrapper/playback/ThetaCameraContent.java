package net.osdn.gokigen.pkremote.camera.vendor.theta.wrapper.playback;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ThetaCameraContent implements ICameraContent
{
    private final String TAG = toString();

    private final String path;
    private final String name;
    private final String url;
    private final String size;
    private final String date;

    ThetaCameraContent(String name, String path, String url, String size, String date)
    {
        this.path = path;
        this.name = name;
        this.url = url;
        this.size = size;
        this.date = date;
        // Log.v(TAG, " [" + " " + "] " + name + " " + path + " (" + size + ") " + date + " " + url);
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
        if (url == null)
        {
            return (getContentPathFromPath());
        }
        return (getContentPathFromUrl());
    }

    private String getContentPathFromPath()
    {
        // データ内容 : "100RICOH/R0010015.JPG"
        try
        {
            int index = path.indexOf("/");
            return (path.substring(0, index));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (path);
    }

    private String getContentPathFromUrl()
    {
        // データ内容 : "http://192.168.1.1/files/abcde/100RICOH/R0010032.JPG"
        try
        {
            int index = url.indexOf("/");
            return (url.substring(0, index));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (url);
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
            return ((target.endsWith("dng")));
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
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss", Locale.ENGLISH);
                return (dateFormat.parse(date));
            } catch (Exception eee) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd hh:mm:ssZ", Locale.ENGLISH);
                    return (dateFormat.parse(date));
                } catch (Exception ee) {
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd hh:mm:ssX", Locale.ENGLISH);
                        return (dateFormat.parse(date));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        return (new Date());
    }

    @Override
    public void setCapturedDate(Date date)
    {
        Log.v(TAG, " setCapturedDate() " + date.toString() + " (name : " + name + " , size: " + size + ")");
    }
}
