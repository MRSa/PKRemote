package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

public class CanonImageContentInfo implements ICameraContent
{
    private final String TAG = toString();
    private final int indexNumber;
    private final String contentPath;
    private boolean isDateValid;
    private Date date;
    private final byte[] rx_body;

    public CanonImageContentInfo(int indexNumber, String contentPath, byte[] binaryData, int offset, int length)
    {
        this.indexNumber = indexNumber;
        this.contentPath = contentPath;
        this.rx_body = Arrays.copyOfRange(binaryData, offset, offset + length);
        try
        {
            //  撮影日時を解析
            if (rx_body.length >= 0x33)
            {
                long objectDate = (rx_body[0x30] & 0xff) + ((rx_body[0x31] & 0xff) << 8);
                objectDate = objectDate + ((rx_body[0x32] & 0xff) << 16) + ((rx_body[0x33] & 0xff) << 24);

                //  UTC から 端末のタイムゾーンに変換する（オフセット時間をとる）
                TimeZone tz = TimeZone.getDefault();
                Date now = new Date();
                long offsetFromUtc = tz.getOffset(now.getTime());

                date = new Date(objectDate * 1000 - offsetFromUtc);
                isDateValid = true;
                return;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        date = new Date();
        isDateValid = false;
        Log.v(TAG, "  > CONTENT : " + date + " ");
    }

    @Override
    public String getCameraId()
    {
        return ("Canon");
    }

    @Override
    public String getCardId()
    {
        return ("sd1");
    }

    @Override
    public String getContentPath()
    {
        return (contentPath);
    }

    @Override
    public String getContentName()
    {
        try
        {
            if (rx_body.length > 0x20)
            {
                int copySize = (rx_body.length < (0x20 + 8 + 3)) ? rx_body.length : (0x20 + 8 + 1 + 3);
                byte[] fileNameArray = Arrays.copyOfRange(rx_body, 0x20, copySize);
                return (new String(fileNameArray));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.v(TAG, "    > File Name : " + indexNumber + ".JPG");
        return ("" + indexNumber + ".JPG");
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
            return ((target.endsWith("crw")) || (target.endsWith("cr2")) || (target.endsWith("cr3")));
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
        return (isDateValid);
    }

    @Override
    public boolean isContentNameValid()
    {
        return (true);
    }

    @Override
    public Date getCapturedDate()
    {
        return (date);
    }

    @Override
    public void setCapturedDate(Date date)
    {
        try
        {
            this.date = date;
            isDateValid = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public int getId()
    {
        return (indexNumber);
    }

    public int getOriginalSize()
    {
        try
        {
            return((rx_body[0x14] & 0xff) + ((rx_body[0x15] & 0xff) << 8) +
                    ((rx_body[0x16] & 0xff) << 16) + ((rx_body[0x17] & 0xff) << 24));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // ちょっと大きめサイズを返す
        return (0x02800000);
    }

    int getStorageId()
    {
        try
        {
            return ((rx_body[4] & 0xff) + ((rx_body[5] & 0xff) << 8) +
                     ((rx_body[6] & 0xff) << 16) + ((rx_body[7] & 0xff) << 24));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (0x00010001);
    }
}
