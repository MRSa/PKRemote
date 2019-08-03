package net.osdn.gokigen.pkremote.camera.interfaces.status;

import java.util.List;

/**
 *
 *
 */
public interface ICameraChangeListener
{
    void onApiListModified(List<String> apis);
    void onCameraStatusChanged(String status);
    void onLiveviewStatusChanged(boolean status);
    void onShootModeChanged(String shootMode);
    void onZoomPositionChanged(int zoomPosition);
    void onStorageIdChanged(String storageId);
    void onFocusStatusChanged(String focusStatus);
    void onResponseError();
}
