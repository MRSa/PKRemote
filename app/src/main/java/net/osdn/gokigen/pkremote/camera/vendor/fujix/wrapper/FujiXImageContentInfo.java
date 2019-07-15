package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;
import java.util.Date;

public class FujiXImageContentInfo implements ICameraContent, IFujiXCommandCallback
{
    private final String TAG = toString();
    private final int indexNumber;
    private boolean isReceived = false;
    private Date date = null;
    private byte[] rx_body;
    FujiXImageContentInfo(int indexNumber, byte[] rx_body)
    {
        this.indexNumber = indexNumber;
        this.rx_body = rx_body;
        if (this.rx_body != null)
        {
            isReceived = true;
        }
    }

    @Override
    public String getCameraId()
    {
        return ("FujiX");
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
        if (isReceived)
        {
            return ("" + indexNumber + ".JPG");
        }
        return ("" + indexNumber + ".JPG");
    }

    @Override
    public Date getCapturedDate()
    {
        if (isReceived)
        {
            return (new Date());
        }
        return (new Date());
    }

    @Override
    public void setCapturedDate(Date date)
    {
        try
        {
            this.date = date;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        Log.v(TAG, "RX : " + indexNumber + "(" + id + ") " + rx_body.length + " bytes.");
        this.rx_body = rx_body;
        isReceived = true;
        updateCapturedDate(rx_body);

    }

    private void updateCapturedDate(byte[] rx_body)
    {
        try
        {
            if (rx_body.length >= 166)
            {
                // データの切り出し
                String fileNameString = new String(pickupString(rx_body, 65, 12));
                String dateString = new String(pickupString(rx_body, 92, 15));
                //char orientation = Character.(rx_body[151]);
                Log.v(TAG, "[" + indexNumber + "] FILE NAME : " + fileNameString + "  DATE : '" + dateString + "'");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

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
