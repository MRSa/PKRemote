package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.playback;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SonyImageContentInfo implements ICameraContent
{
    private final String TAG = toString();
    private final JSONObject contentObject;
    private final String contentString;

/*
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
    private String remotePlayType = "";
*/

    SonyImageContentInfo(JSONObject contentObject, String contentString)
    {
        this.contentObject = contentObject;  // 応答性能向上のため、データの保持方法を変える
        this.contentString = contentString;
/*
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
*/
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
        if (contentObject != null)
        {
            return (getContentPathObject());
        }
        return (getContentPathString());
    }

    private String getContentPathObject()
    {
        return (getObjectString(contentObject, "folderNo"));
    }

    private String getContentPathString()
    {
        return ("");
    }

    @Override
    public String getContentName()
    {
        if (contentObject != null)
        {
            return (getContentNameObject());
        }
        return (getContentNameString());
    }

    private String getContentNameObject()
    {
        String fileName = "";
        try
        {
            JSONObject contents = contentObject.getJSONObject("content");
            JSONObject originalObject = contents.getJSONArray("original").getJSONObject(0);
            fileName = getObjectString(originalObject, "fileName");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (fileName);
    }

    private String getContentNameString()
    {
        try
        {
            int startIndex = contentString.indexOf("<dc:title>");
            int endIndex = contentString.indexOf("</dc:title>");
            Log.v(TAG, "getContentNameString : " + contentString.substring(startIndex + 10, endIndex));
            return (contentString.substring(startIndex + 10, endIndex));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("");
    }

    @Override
    public boolean isDateValid()
    {
        return (true);
    }

    @Override
    public Date getCapturedDate()
    {
        if (contentObject != null)
        {
            return (getCapturedDateObject());
        }
        return (getCapturedDateString());
    }

    private Date getCapturedDateObject()
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

    private Date getCapturedDateString()
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

/*
    String getContentKind()
    {
        return (getObjectString(contentObject,"contentKind"));
    }

    String getStillObject()
    {
        String stillObject = "";
        try
        {
            JSONObject contents = contentObject.getJSONObject("content");
            JSONObject originalObject = contents.getJSONArray("original").getJSONObject(0);
            stillObject = getObjectString(originalObject, "stillObject");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (stillObject);
    }
*/

    String getOriginalUrl()
    {
        if (contentObject != null)
        {
            return (getOriginalUrlObject());
        }
        return (getOriginalUrlString());
    }

    private String getOriginalUrlString()
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

    private String getOriginalUrlObject()
    {
        String url = "";
        try
        {
            JSONObject contents = contentObject.getJSONObject("content");
            JSONObject originalObject = contents.getJSONArray("original").getJSONObject(0);
            url = getObjectString(originalObject, "url");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (url);
    }

    String getLargeUrl()
    {
        if (contentObject != null)
        {
            return (getLargeUrlObject());
        }
        return (getLargeUrlString());
    }

    private String getLargeUrlString()
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


    private String getLargeUrlObject()
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

    String getSmallUrl()
    {
        if (contentObject != null)
        {
            return (getSmallUrlObject());
        }
        return (getSmallUrlString());
    }

    private String getSmallUrlString()
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

    private String getSmallUrlObject()
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

    String getThumbnailUrl()
    {
        if (contentObject != null)
        {
            return (getThumbnailUrlObject());
        }
        return (getThumbnailUrlString());
    }

    private String getThumbnailUrlString()
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

    private String getThumbnailUrlObject()
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
