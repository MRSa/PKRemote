package net.osdn.gokigen.pkremote.camera.vendor.olympuspen.wrapper.playback;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.util.ArrayList;
import java.util.List;

/**
 *   画像一覧について、受信データを解析して保持するクラス
 *
 */
class OlympusPenObjectDataHolder
{
    private List<ICameraContent> contentList;

    OlympusPenObjectDataHolder()
    {
        contentList = new ArrayList<>();
    }

    void clear()
    {
        contentList.clear();
    }

    List<OlympusPenCameraContent> parsePath(String response)
    {
        List<OlympusPenCameraContent> pathList = new ArrayList<>();
        try
        {
            if ((response == null) || (response.length() <= 0))
            {
                // データがない、、
                return (pathList);
            }

            for (String path : response.split("\r\n"))
            {
                String[] values = path.split(",");
                if (values.length > 5)
                {
                    pathList.add(new OlympusPenCameraContent(values[0], values[1], values[2], values[3], values[4], values[5]));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (pathList);
    }

    void parseImage(String response)
    {
        try
        {
            if ((response == null) || (response.length() <= 0))
            {
                // データがない、、何もしない
                return;
            }

            for (String path : response.split("\r\n"))
            {
                String[] values = path.split(",");
                if (values.length > 5)
                {
                    contentList.add(new OlympusPenCameraContent(values[0], values[1], values[2], values[3], values[4], values[5]));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    List<ICameraContent> getImageList()
    {
        return (contentList);
    }

}
