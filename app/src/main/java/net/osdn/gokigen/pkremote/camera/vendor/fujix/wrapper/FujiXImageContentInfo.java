package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;

import java.util.Arrays;
import java.util.Date;

public class FujiXImageContentInfo implements ICameraContent, IFujiXCommandCallback
{
    private final String TAG = toString();
    private final int indexNumber;
    private boolean isReceived = false;
    byte[] rx_body;
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
        return null;
    }

    @Override
    public String getCardId()
    {
        return null;
    }

    @Override
    public String getContentPath()
    {
        return null;
    }

    @Override
    public String getContentName()
    {
        return null;
    }

    @Override
    public Date getCapturedDate()
    {
        if (isReceived)
        {
        }
        return null;
    }

    @Override
    public void setCapturedDate(Date date)
    {

    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        Log.v(TAG, "RX : " + indexNumber + "(" + id + ") " + rx_body.length + " bytes.");
        this.rx_body = rx_body;
        isReceived = true;

    }
}
