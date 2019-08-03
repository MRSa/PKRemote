package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.eventlistener;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;

import java.util.List;

public class CameraChangeListerTemplate implements ICameraChangeListener
{
    private static final String TAG = CameraChangeListerTemplate.class.getSimpleName();

    public CameraChangeListerTemplate()
    {
        Log.v(TAG, "CameraChangeListerTemplate");

    }

    @Override
    public void onApiListModified(List<String> apis)
    {
        Log.v(TAG, "onApiListModified() : " + apis.size());
    }

    @Override
    public void onCameraStatusChanged(String status)
    {
        Log.v(TAG, "onCameraStatusChanged() : " + status);
    }

    @Override
    public void onLiveviewStatusChanged(boolean status)
    {
        Log.v(TAG, "onLiveviewStatusChanged() : " + status);
    }

    @Override
    public void onShootModeChanged(String shootMode)
    {
        Log.v(TAG, "onShootModeChanged() : " + shootMode);
    }

    @Override
    public void onZoomPositionChanged(int zoomPosition)
    {
        Log.v(TAG, "onZoomPositionChanged() : " + zoomPosition);
    }

    @Override
    public void onStorageIdChanged(String storageId)
    {
        Log.v(TAG, "onStorageIdChanged() : " + storageId);
    }

    @Override
    public void onFocusStatusChanged(String focusStatus)
    {
        Log.v(TAG, "onFocusStatusChanged() : " + focusStatus);
    }

    @Override
    public void onResponseError()
    {
        Log.v(TAG, "onResponseError() ");
    }
}
