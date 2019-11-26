package net.osdn.gokigen.pkremote.camera.interfaces.playback;

import java.util.Date;

public interface ICameraContent
{
    String getCameraId();
    String getCardId();
    String getContentPath();
    String getContentName();
    String getOriginalName();
    boolean isRaw();
    boolean isMovie();
    boolean isDateValid();
    boolean isContentNameValid();
    Date getCapturedDate();
    void setCapturedDate(Date date);
}
