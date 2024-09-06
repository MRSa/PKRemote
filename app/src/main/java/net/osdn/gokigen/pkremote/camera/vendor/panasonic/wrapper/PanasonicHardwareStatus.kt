package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraHardwareStatus

class PanasonicHardwareStatus : ICameraHardwareStatus
{
    override fun isAvailableHardwareStatus(): Boolean { return (false) }
    override fun getLensMountStatus(): String? { return (null) }
    override fun getMediaMountStatus(): String? { return (null) }
    override fun getMinimumFocalLength(): Float { return (0.0f) }
    override fun getMaximumFocalLength(): Float { return (0.0f) }
    override fun getActualFocalLength(): Float { return (0.0f) }
    override fun inquireHardwareInformation(): Map<String, Any>? { return (null) }
}