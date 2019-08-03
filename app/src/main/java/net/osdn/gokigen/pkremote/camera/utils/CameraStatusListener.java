package net.osdn.gokigen.pkremote.camera.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;

import java.util.List;

public class CameraStatusListener implements ICameraStatusUpdateNotify, ICameraChangeListener
{
    private  final String TAG = toString();
    private ICameraStatusUpdateNotify updateReceiver = null;

    public CameraStatusListener()
    {
        Log.v(TAG, "CameraStatusListener()");
    }

    void setUpdateReceiver(@NonNull ICameraStatusUpdateNotify receiver)
    {
        updateReceiver = receiver;
    }

    @Override
    public void updatedTakeMode(String mode)
    {
        Log.v(TAG, "updatedTakeMode() : " + mode);

    }

    @Override
    public void updatedShutterSpeed(String tv)
    {
        Log.v(TAG, "updatedShutterSpeed() : " + tv);
    }

    @Override
    public void updatedAperture(String av)
    {
        Log.v(TAG, "updatedAperture() : " + av);
    }

    @Override
    public void updatedExposureCompensation(String xv)
    {
        Log.v(TAG, "updatedExposureCompensation() : " + xv);
    }

    @Override
    public void updatedMeteringMode(String meteringMode)
    {
        Log.v(TAG, "updatedMeteringMode() : " + meteringMode);

    }

    @Override
    public void updatedWBMode(String wbMode)
    {
        Log.v(TAG, "updatedWBMode() : " + wbMode);
    }

    @Override
    public void updateRemainBattery(int percentage)
    {
        Log.v(TAG, "updateRemainBattery() : " + percentage + "%");

    }

    @Override
    public void updateFocusedStatus(boolean focused, boolean focusLocked)
    {
        Log.v(TAG, "" + focused + " (" + focusLocked + ")");
        if (updateReceiver != null)
        {
            updateReceiver.updateFocusedStatus(focused, focusLocked);
        }
    }

    @Override
    public void updateIsoSensitivity(String sv)
    {
        Log.v(TAG, "updateIsoSensitivity() : " + sv);
    }

    @Override
    public void updateWarning(String warning)
    {
        Log.v(TAG, "updateWarning() : " + warning);
    }

    @Override
    public void updateStorageStatus(String status)
    {
        Log.v(TAG, "updateStorageStatus() : " + status);
    }

    @Override
    public void onApiListModified(List<String> apis)
    {
        Log.v(TAG, "onApiListModified() : ");
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
        Log.v(TAG, "onResponseError() : ");
    }
}
