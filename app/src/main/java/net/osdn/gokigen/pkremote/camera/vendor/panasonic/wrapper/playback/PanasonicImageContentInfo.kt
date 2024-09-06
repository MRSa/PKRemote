package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.playback

import android.util.Log
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent
import java.util.Date
import java.util.Locale

class PanasonicImageContentInfo internal constructor(private val targetUrl: String) : ICameraContent
{
    private var date = Date()
    private var isDateValid = false

    override fun getCameraId(): String
    {
        return ""
    }

    override fun getCardId(): String
    {
        return ""
    }

    override fun getContentPath(): String
    {
        return ""
    }

    override fun getContentName(): String
    {
        return (targetUrl.substring(targetUrl.lastIndexOf("/") + 1))
    }

    override fun getOriginalName(): String
    {
        return (contentName)
    }

    override fun isRaw(): Boolean
    {
        try
        {
            val target = contentName.lowercase(Locale.getDefault())
            return ((target.endsWith("rw2")) || (target.endsWith("raw")))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    override fun isMovie(): Boolean
    {
        try
        {
            val target = contentName.lowercase(Locale.getDefault())
            return ((target.endsWith("mov")) || (target.endsWith("mp4")))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    override fun isDateValid(): Boolean
    {
        return (isDateValid)
    }

    override fun isContentNameValid(): Boolean
    {
        return (true)
    }

    override fun getCapturedDate(): Date
    {
        return (date)
    }

    override fun setCapturedDate(date: Date)
    {
        Log.v(TAG, "setCapturedDate()")
        this.date = date
        isDateValid = true
    }

    companion object
    {
        private val TAG: String = PanasonicImageContentInfo::class.java.simpleName
    }
}
