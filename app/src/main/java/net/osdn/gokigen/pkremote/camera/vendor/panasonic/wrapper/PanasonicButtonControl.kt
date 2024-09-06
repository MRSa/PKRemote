package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl

class PanasonicButtonControl : ICameraButtonControl {
    override fun pushedButton(code: String, isLongPress: Boolean): Boolean {
        return (false)
    }
}