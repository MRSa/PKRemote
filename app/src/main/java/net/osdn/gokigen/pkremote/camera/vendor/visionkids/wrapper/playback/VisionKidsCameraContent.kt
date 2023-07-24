package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.playback

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VisionKidsCameraContent(private val name: String, private val path: String, private val dateString: String) : ICameraContent
{
    override fun getCameraId(): String { return ("") }

    override fun getCardId(): String { return ("") }

    override fun getContentPath(): String { return (path) }

    override fun getContentName(): String { return (name) }

    override fun getOriginalName(): String { return (name) }

    override fun isRaw(): Boolean { return (false) }

    override fun isMovie(): Boolean { return (name.contains(".AVI")) }

    override fun isDateValid(): Boolean { return (true) }

    override fun isContentNameValid(): Boolean { return (true) }

    override fun getCapturedDate(): Date
    {
        try
        {
            val dateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
            return (dateFormat.parse(dateString)?: Date())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (Date())
    }

    override fun setCapturedDate(date: Date?) { }

    companion object
    {
        private val TAG = VisionKidsCameraContent::class.java.simpleName
    }
}
