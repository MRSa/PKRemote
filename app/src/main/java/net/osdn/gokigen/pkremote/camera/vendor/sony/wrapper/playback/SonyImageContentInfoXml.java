package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.playback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class SonyImageContentInfoXml implements ISonyImageContentInfo
{
    private final String contentString;

    SonyImageContentInfoXml(String contentString)
    {
        this.contentString = contentString;
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
        return ("");
    }

    @Override
    public String getContentName()
    {
        try
        {
            int startIndex = contentString.indexOf("<dc:title>");
            int endIndex = contentString.indexOf("</dc:title>");
            // Log.v(TAG, "  getContentNameString : " + contentString.substring(startIndex + 10, endIndex));
            return (contentString.substring(startIndex + 10, endIndex));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("");
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
            return ((target.endsWith("arw")));
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
            int startIndex = contentString.indexOf("<dc:date>");
            int endIndex = contentString.indexOf("</dc:date>");
            String createTime = contentString.substring(startIndex + 9, endIndex);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            return (dateFormat.parse(createTime));
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
        // 設定変更できません
    }

    @Override
    public String getOriginalUrl()
    {
        try
        {
            int startIndex = contentString.indexOf("image/jpeg:*");
            int httpStringStart = contentString.indexOf(">http:", startIndex);
            int httpStringEnd = contentString.indexOf("<", httpStringStart);
            return (contentString.substring((httpStringStart + 1), (httpStringEnd - 1)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("");
    }

    @Override
    public String getLargeUrl()
    {
        try
        {
            int startIndex = contentString.indexOf("JPEG_LRG");
            int httpStringStart = contentString.indexOf(">http:", startIndex);
            int httpStringEnd = contentString.indexOf("<", httpStringStart);
            return (contentString.substring((httpStringStart + 1), (httpStringEnd - 1)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("");
    }

    @Override
    public String getSmallUrl()
    {
        try
        {
            int startIndex = contentString.indexOf("JPEG_SM");
            int httpStringStart = contentString.indexOf(">http:", startIndex);
            int httpStringEnd = contentString.indexOf("<", httpStringStart);
            return (contentString.substring((httpStringStart + 1), (httpStringEnd - 1)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("");
    }

    @Override
    public String getThumbnailUrl()
    {
        try
        {
            int startIndex = contentString.indexOf("JPEG_TN");
            int httpStringStart = contentString.indexOf(">http:", startIndex);
            int httpStringEnd = contentString.indexOf("<", httpStringStart);
            return (contentString.substring((httpStringStart + 1), (httpStringEnd - 1)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("");
    }

}
