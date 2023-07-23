package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper

interface IFtpServiceCallback
{
    fun onReceivedFtpResponse(command: String, code: Int, response: String)
}