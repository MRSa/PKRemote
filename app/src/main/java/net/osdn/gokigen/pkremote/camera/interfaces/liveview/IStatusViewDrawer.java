package net.osdn.gokigen.pkremote.camera.interfaces.liveview;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;

public interface IStatusViewDrawer
{
    void updateGridIcon();
    void updateConnectionStatus(ICameraConnection.CameraConnectionStatus connectionStatus);
    void updateStatusView(String message);
    void updateLiveViewScale(boolean isChangeScale);
    void startLiveView();
}
