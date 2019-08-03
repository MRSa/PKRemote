package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.eventlistener;

import java.util.List;

public interface ICameraStatusHolder
{
    String getCameraStatus();
    boolean getLiveviewStatus();
    String getShootMode();
    List<String> getAvailableShootModes();
    int getZoomPosition();
    String getStorageId();

}
