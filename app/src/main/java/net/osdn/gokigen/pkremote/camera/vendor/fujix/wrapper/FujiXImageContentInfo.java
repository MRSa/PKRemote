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
    }
}
