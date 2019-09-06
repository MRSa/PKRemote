package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.playback;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SonyImageContentInfo implements ICameraContent
{
    private final String TAG = toString();

    private String uri = "";
    private String title = "";
    // content
    //  original
    private String url = "";    // Original content URL
    private String fileName = "";
    private String stillObject = "";  // jpeg/raw/mpo/""
    private String largeUrl = "";   // 2M pixel
    private String smallUrl = "";   // VGA
    private String thumbnailUrl = "";
    private Date createdTime = null;
    private String contentKind = "";
    private String folderNo = "";
    private String fileNo = "";
    private String isPlayable = "";
    private String isBrowsable = "";
    private String isProtected = "";
    //  remotePlayType

    SonyImageContentInfo(JSONObject contentObject)
    {
        try
        {
            title = getObjectString(contentObject, "title");
            isPlayable = getObjectString(contentObject, "isPlayable");
            folderNo = getObjectString(contentObject, "folderNo");
            isBrowsable = getObjectString(contentObject, "isBrowsable");
            fileNo = getObjectString(contentObject, "fileNo");
            isProtected = getObjectString(contentObject, "isProtected");
            uri = getObjectString(contentObject, "uri");
            contentKind = getObjectString(contentObject,"contentKind");
            createdTime = getDateTime(contentObject, "createdTime");

            JSONObject contents = contentObject.getJSONObject("content");
            smallUrl = getObjectString(contents, "smallUrl");
            largeUrl = getObjectString(contents, "largeUrl");
            thumbnailUrl =getObjectString(contents, "thumbnailUrl");

            JSONObject originalObject = contents.getJSONArray("original").getJSONObject(0);
            fileName = getObjectString(originalObject, "fileName");
            stillObject = getObjectString(originalObject, "stillObject");
            url = getObjectString(originalObject, "url");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private Date getDateTime(JSONObject content, String name)
    {
        try
        {
            String createTime = getObjectString(content, name);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH);
            return (dateFormat.parse(createTime));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new Date());
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
        return (folderNo);
    }

    @Override
    public String getContentName()
    {
        return (fileName);
    }

    @Override
    public boolean isDateValid()
    {
        return (true);
    }

    @Override
    public Date getCapturedDate()
    {
        return (createdTime);
    }

    @Override
    public void setCapturedDate(Date date)
    {
        // 設定変更できません
    }


    String getContentKind()
    {
        return (contentKind);
    }

    String getStillObject()
    {
        return (stillObject);
    }

    String getOriginalUrl()
    {
        return (url);
    }

    String getLargeUrl()
    {
        return (largeUrl);
    }

    String getSmallUrl()
    {
        return (smallUrl);
    }

    String getThumbnailUrl()
    {
        return (thumbnailUrl);
    }
}
