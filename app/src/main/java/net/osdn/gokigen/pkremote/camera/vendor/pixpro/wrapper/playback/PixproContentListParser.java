package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.playback;

import android.util.Xml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

class PixproContentListParser
{
    //private final String TAG = toString();
    private static final String CAMERACONTENT_TAG = "File";
    private static final String PATHROOT_TAG = "PATHROOT";
    private static final String NAME_TAG = "NAME";
    private static final String FPATH_TAG = "FPATH";
    private static final String SIZE_TAG = "SIZE";
    private static final String TIMECODE_TAG = "TIMECODE";
    private static final String DCFINDEX_TAG = "DCFINDEX";
    private static final String ATTR_TAG = "ATTR";

    private String pathRoot = null;
//    private List<PixproCameraContent> contentList;

    PixproContentListParser()
    {
        //contentList = new ArrayList<>();
    }

    public List<ICameraContent> parseContentList(@NonNull String receivedMessage)
    {
        // 受信したボディを解析して、画像一覧を cameraContentList に入れる
        List<ICameraContent> cameraContentList = new ArrayList<>();
        PixproCameraContent cameraContent = null;
        //contentList.clear();
        try
        {
            String tagName = null;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(receivedMessage));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if(eventType == XmlPullParser.START_DOCUMENT)
                {
                    System.out.println("Start document");
                }
                else if(eventType == XmlPullParser.START_TAG)
                {
                    tagName = parser.getName();
                }
                else if(eventType == XmlPullParser.END_TAG)
                {
                    String tag = parser.getName();
                    if (tag != null)
                    {
                        if (tag.matches(CAMERACONTENT_TAG))
                        {
                            //contentList.add(cameraContent);
                            cameraContentList.add(cameraContent);
                            cameraContent = null;
                        }
                        //Log.v(TAG, "  ----- END TAG : " + tag + "  ------ ");
                    }
                    tagName = null;  // parser.getName();
                }
                else if(eventType == XmlPullParser.TEXT)
                {
                    String content = parser.getText();
                    if (tagName != null)
                    {
                        if (tagName.matches(CAMERACONTENT_TAG))
                        {
                            cameraContent = new PixproCameraContent(pathRoot);
                        }
                        else
                        parseData(tagName, content, cameraContent);
                    }
                }
                eventType = parser.next();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (cameraContentList);
    }

    private void parseData(@NonNull String tagName, @Nullable String content, @Nullable PixproCameraContent cameraContent)
    {
        //Log.v(TAG, "  ----- " + tagName + " : " + content + "  ------ ");
        try
        {
            if (tagName.matches(PATHROOT_TAG))
            {
                pathRoot = content;
            }
            if (cameraContent != null)
            {
                if (tagName.matches(NAME_TAG))
                {
                    cameraContent.setObjName(content);
                }
                else if (tagName.matches(FPATH_TAG))
                {
                    cameraContent.setFilePath(content);
                }
                else if (tagName.matches(SIZE_TAG))
                {
                    cameraContent.setObjSize(content);
                }
                else if (tagName.matches(TIMECODE_TAG))
                {
                    cameraContent.setDateTime(content);
                }
                else if (tagName.matches(DCFINDEX_TAG))
                {
                    cameraContent.setDcfIndex(content);
                }
                else if (tagName.matches(ATTR_TAG))
                {
                    cameraContent.setAttr(content);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}