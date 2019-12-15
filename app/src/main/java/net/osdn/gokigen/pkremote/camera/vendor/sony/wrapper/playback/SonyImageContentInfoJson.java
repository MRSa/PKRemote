package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.playback;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SonyImageContentInfoJson implements ISonyImageContentInfo
{
    //private final String TAG = toString();
    private final JSONObject contentObject;
    private final int objectIndex;

    SonyImageContentInfoJson(JSONObject contentObject, int objectIndex)
    {
        this.contentObject = contentObject;
        this.objectIndex = objectIndex;
    }

    private String getObjectString(JSONObject target, String name)
    {
        String value = "";
        try
        {
            value = target.getString(name);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (value);
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
        return (getObjectString(contentObject, "folderNo"));
    }

    @Override
    public String getContentName()
    {
        String fileName = "";
        try
        {
            JSONObject contents = contentObject.getJSONObject("content");
            JSONObject originalObject = contents.getJSONArray("original").getJSONObject(objectIndex);
            fileName = getObjectString(originalObject, "fileName");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (fileName);
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
            String createTime = getObjectString(contentObject, "createdTime");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH);
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
        String url = "";
        try
        {
            JSONObject contents = contentObject.getJSONObject("content");
            JSONObject originalObject = contents.getJSONArray("original").getJSONObject(objectIndex);
            url = getObjectString(originalObject, "url");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (url);
    }

    @Override
    public String getLargeUrl()
    {
        String largeUrl = "";
        try
        {
            JSONObject contents = contentObject.getJSONObject("content");
            largeUrl = getObjectString(contents, "largeUrl");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (largeUrl);
    }

    @Override
    public String getSmallUrl()
    {
        String smallUrl = "";
        try
        {
            JSONObject contents = contentObject.getJSONObject("content");
            smallUrl = getObjectString(contents, "smallUrl");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (smallUrl);
    }

    @Override
    public String getThumbnailUrl()
    {
        String thumbnailUrl = "";
        try
        {
            JSONObject contents = contentObject.getJSONObject("content");
            thumbnailUrl = getObjectString(contents, "thumbnailUrl");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (thumbnailUrl);
    }
}
