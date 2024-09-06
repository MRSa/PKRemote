package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper

interface IPanasonicCamera {
    fun hasApiService(serviceName: String?): Boolean
    fun getApiServices(): List<IPanasonicApiService?>?

    fun getFriendlyName(): String?
    fun getModelName(): String?

    fun getDdUrl(): String?
    fun getCmdUrl(): String?
    fun getObjUrl(): String?
    fun getPictureUrl(): String?

    fun getClientDeviceUuId(): String

    fun getCommunicationSessionId() : String?
    fun setCommunicationSessionId(sessionId: String?)
}