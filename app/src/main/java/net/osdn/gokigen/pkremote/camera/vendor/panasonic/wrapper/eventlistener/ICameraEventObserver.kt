package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener

interface ICameraEventObserver {
    fun activate()
    fun start(): Boolean
    fun stop()
    fun release()

    fun setEventListener(listener: ICameraChangeListener)
    fun clearEventListener()

    fun getCameraStatusHolder(): ICameraStatusHolder?
}