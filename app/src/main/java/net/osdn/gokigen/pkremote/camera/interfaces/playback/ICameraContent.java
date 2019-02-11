package net.osdn.gokigen.pkremote.camera.interfaces.playback;

import java.util.Date;

public interface ICameraContent
{
    String getCameraId();
    String getCardId();
    String getContentPath();
    String getContentName();
    Date getCapturedDate();
    void setCapturedDate(Date date);
}
