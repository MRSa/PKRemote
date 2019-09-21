package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.utils.SimpleLogDumper;

import java.util.Arrays;
import java.util.Date;



/*
  --- CONTENT (IMAGE OBJECTS) ---
  0000:91 99 b8 01 01 00 01 00 01 38 00 00 00 00 00 00
  0010:20 00 00 00 69 42 58 00 00 00 b8 01 90 99 b8 01
  0020:49 4d 47 5f 32 34 35 37 2e 4a 50 47 00 00 00 00
  0030:e6 6b 86 5d
*/

public class PtpIpImageContentInfo implements ICameraContent
{
    private final String TAG = toString();
    private final int indexNumber;
    private boolean isDateValid;
    private Date date;
    private String realFileName;
    private byte[] rx_body;

    PtpIpImageContentInfo(int indexNumber, byte[] binaryData, int offset, int length)
    {
        this.indexNumber = indexNumber;
        this.rx_body = Arrays.copyOfRange(binaryData, offset, offset + length);
        Log.v(TAG, " --- CONTENT ---");
        SimpleLogDumper.dump_bytes(" [(" + length + ")] ", this.rx_body);

        //  動作用...
        date = new Date();
        isDateValid = false;
        realFileName = "";
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
        return ("");
    }

    @Override
    public String getContentName()
    {
        try
        {
            if ((realFileName != null)&&(realFileName.contains(".MOV")))
            {
                return ("" + indexNumber + ".MOV");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("" + indexNumber + ".JPG");
    }

    @Override
    public boolean isDateValid()
    {
        return (isDateValid);
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

/*
    public boolean isReceived()
    {
        return (isReceived);
    }

    private void updateInformation(byte[] rx_body)
    {
        try
        {
            if (rx_body.length >= 166)
            {
                // データの切り出し
                realFileName = new String(pickupString(rx_body, 65, 12));
                String dateString = new String(pickupString(rx_body, 92, 15));
                //char orientation = Character.(rx_body[151]);
                Log.v(TAG, "[" + indexNumber + "] FILE NAME : " + realFileName + "  DATE : '" + dateString + "'");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.ENGLISH);
                date = dateFormat.parse(dateString);
                isDateValid = true;
                isReceived = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

 */
    /**
     *   文字列を無理やり切り出す...
     *
     */
    private byte[] pickupString(byte[] data, int start, int length)
    {
        byte[] result = new byte[length];
        for (int index = 0; index < length; index++)
        {
            result[index] = data[start + index * 2];
        }
        return (result);
    }
}
