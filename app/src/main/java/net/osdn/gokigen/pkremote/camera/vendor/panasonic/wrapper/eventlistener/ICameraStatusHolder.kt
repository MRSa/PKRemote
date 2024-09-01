package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener

interface ICameraStatusHolder
{
    fun getCameraStatus(): String?
    fun getLiveviewStatus(): Boolean
    fun getShootMode(): String?
    fun getAvailableShootModes(): List<String?>?
    fun getZoomPosition(): Int
    fun getStorageId(): String?
}