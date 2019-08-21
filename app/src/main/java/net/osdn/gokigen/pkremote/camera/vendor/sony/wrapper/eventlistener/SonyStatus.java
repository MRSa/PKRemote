package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.eventlistener;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher;

import java.util.List;

public class SonyStatus  implements ICameraStatusUpdateNotify, ICameraStatusWatcher, ICameraStatus
{
    @Override
    public void updatedTakeMode(String mode) {

    }

    @Override
    public void updatedShutterSpeed(String tv) {

    }

    @Override
    public void updatedAperture(String av) {

    }

    @Override
    public void updatedExposureCompensation(String xv) {

    }

    @Override
    public void updatedMeteringMode(String meteringMode) {

    }

    @Override
    public void updatedWBMode(String wbMode) {

    }

    @Override
    public void updateRemainBattery(int percentage) {

    }

    @Override
    public void updateFocusedStatus(boolean focused, boolean focusLocked) {

    }

    @Override
    public void updateIsoSensitivity(String sv) {

    }

    @Override
    public void updateWarning(String warning) {

    }

    @Override
    public void updateStorageStatus(String status) {

    }

    @Override
    public List<String> getStatusList(String key) {
        return null;
    }

    @Override
    public String getStatus(String key) {
        return null;
    }

    @Override
    public void setStatus(String key, String value) {

    }

    @Override
    public void startStatusWatch(ICameraStatusUpdateNotify notifier) {

    }

    @Override
    public void stopStatusWatch() {

    }
}
