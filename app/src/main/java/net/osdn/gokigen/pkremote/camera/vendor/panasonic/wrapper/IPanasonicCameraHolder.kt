package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener

interface IPanasonicCameraHolder
{
    fun detectedCamera(camera: IPanasonicCamera?)
    fun prepare()
    fun startRecMode()
    fun startPlayMode()
    fun startEventWatch(listener: ICameraChangeListener?)
}
