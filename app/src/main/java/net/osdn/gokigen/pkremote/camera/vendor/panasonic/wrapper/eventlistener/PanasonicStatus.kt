package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraStatusUpdateNotify
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher

class PanasonicStatus : ICameraStatusUpdateNotify, ICameraStatusWatcher, ICameraStatus {
    override fun updatedTakeMode(mode: String) {
    }

    override fun updatedShutterSpeed(tv: String) {
    }

    override fun updatedAperture(av: String) {
    }

    override fun updatedExposureCompensation(xv: String) {
    }

    override fun updatedMeteringMode(meteringMode: String) {
    }

    override fun updatedWBMode(wbMode: String) {
    }

    override fun updateRemainBattery(percentage: Int) {
    }

    override fun updateFocusedStatus(focused: Boolean, focusLocked: Boolean) {
    }

    override fun updateIsoSensitivity(sv: String) {
    }

    override fun updateWarning(warning: String) {
    }

    override fun updateStorageStatus(status: String) {
    }

    override fun getStatusList(key: String): List<String>? {
        return null
    }

    override fun getStatus(key: String): String? {
        return null
    }

    override fun setStatus(key: String, value: String) {
    }

    override fun startStatusWatch(notifier: ICameraStatusUpdateNotify) {
    }

    override fun stopStatusWatch() {
    }
}