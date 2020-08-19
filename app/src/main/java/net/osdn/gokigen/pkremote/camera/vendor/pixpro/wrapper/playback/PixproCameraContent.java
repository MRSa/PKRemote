package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.playback;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class PixproCameraContent implements ICameraContent
{
    //private final String TAG = toString();
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
        return (false);
    }

    @Override
    public Date getCapturedDate()
    {
        int dateTimeInt = 0;
        try
        {
            // === 年月日時分秒の数値情報変換 (文字列からint型数値へ) ===
            // === int型数値：4バイト表記  上位2バイトが年月日、下位にバイトが時分秒 ===
            //    (MS-DOS : FAT由来のフォーマット)
            // 上位２バイトの年月日情報、10進表記
            //   bit0-4  : 日
            //   bit8-5  : 月
            //   bit15-9 : 年 (+1980する)
            //
            // 下位にバイトの時分秒情報、10進数表記。
            //   bit0-4: 秒/2 (2秒精度)
            //   bit5-10 : 分  (0～59)
            //   bit11-15 : 時 (0～24)
            dateTimeInt = Integer.parseInt(dateTime);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (dateTimeInt > 0)
        {
            // 年月日・時分秒に変換する。
            int dateInt = ((dateTimeInt & 0xffff0000) >> 16);
            int timeInt = (dateTimeInt & 0x0000ffff);

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
        return (new Date());
    }

    @Override
    public void setCapturedDate(Date date)
    {

    }
}
