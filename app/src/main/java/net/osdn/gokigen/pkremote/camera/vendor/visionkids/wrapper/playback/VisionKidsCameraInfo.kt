package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.playback

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo
import java.util.Date

class VisionKidsCameraInfo(private val fileName: String, private val date: Date) : ICameraFileInfo
{
    override fun getDatetime(): Date { return (date) }
    override fun getDirectoryPath(): String { return ("/") }
    override fun getFilename(): String { return (fileName) }
    override fun getAperature(): String { return ("") }
    override fun getShutterSpeed(): String  { return ("") }
    override fun getIsoSensitivity(): String { return ("") }
    override fun getExpRev(): String { return ("") }
    override fun getOrientation(): Int { return (0) }
    override fun getAspectRatio(): String {return ("") }
    override fun getModel(): String { return ("") }
    override fun getLatLng(): String { return ("") }
    override fun getCaptured(): Boolean { return (true) }
    override fun updateValues(
        dateTime: String?,
        av: String?,
        tv: String?,
        sv: String?,
        xv: String?,
        orientation: Int,
        aspectRatio: String?,
        model: String?,
        LatLng: String?,
        captured: Boolean
    ) { }
}
