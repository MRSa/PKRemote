package net.osdn.gokigen.pkremote.camera.playback;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;

import java.util.Date;

public class CameraContentInfo implements ICameraContent
{
    private final String cameraId;
    private final String cardId;
    private final String contentPath;
    private final String contentName;
    private Date capturedDate;

    public CameraContentInfo(String cameraId, String cardId, String contentPath, String contentName, Date date)
    {
        this.cameraId = cameraId;
        this.cardId = cardId;
        this.contentPath = contentPath;
        this.contentName = contentName;
        this.capturedDate = date;

    }

    @Override
    public String getCameraId()
    {
        return (cameraId);
    }

    @Override
    public String getCardId()
    {
        return (cardId);
    }

    @Override
    public String getContentPath()
    {
        return (contentPath);
    }

    @Override
    public String getContentName()
    {
        return (contentName);
    }

    @Override
    public Date getCapturedDate()
    {
        return (capturedDate);
    }

    @Override
    public void setCapturedDate(Date date)
    {
        this.capturedDate = date;
    }
}