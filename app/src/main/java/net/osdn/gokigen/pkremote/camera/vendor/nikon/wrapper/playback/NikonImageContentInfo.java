package net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.playback;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.util.Date;

class NikonImageContentInfo implements ICameraContent
{
    private final String TAG = toString();
    private Date date;
    private String contentName = "";
    private boolean isDateValid = false;
    private boolean isContentNameValid = false;
    private int contentSize = 0;


    private final int storageId;
    private final int subDirectoryId;
    private final int objectId;

    NikonImageContentInfo(int storageId, int subDirectoryId, int objectId)
    {
        this.storageId = storageId;
        this.subDirectoryId = subDirectoryId;
        this.objectId = objectId;
        this.date = new Date();

        //String dumpLog = String.format(" ST: 0x%08x SD: 0x%08x OBJ: 0x%08x", storageId, subDirectoryId, objectId);
        //Log.v(TAG, " NikonImageContentInfo " + dumpLog);
    }

    int getStorageId()
    {
        return (storageId);
    }

    int getSubdirectoryId()
    {
        return (subDirectoryId);
    }

    int getObjectId()
    {
        return (objectId);
    }

    int getOriginalSize()
    {
        return (contentSize);
    }

    @Override
    public String getCameraId()
    {
        return "";
    }

    @Override
    public String getCardId()
    {
        return (String.format("0x%08x", storageId));
    }

    @Override
    public String getContentPath()
    {
        return (String.format("0x%08x", subDirectoryId));
    }

    @Override
    public String getContentName()
    {
        if (isContentNameValid)
        {
            return (contentName);
        }
        return (String.format("0x%08x.JPG", objectId));
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
        Log.v(TAG, "setCapturedDate()");
        this.date = date;
        isDateValid = true;
    }

    public void setContentName(String contentName)
    {
        Log.v(TAG, "setContentName() : " + contentName);
        this.contentName = contentName;
        isContentNameValid = true;
    }

    public void setOriginalSize(int size)
    {
        this.contentSize = size;
    }
}
