package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.playback

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl

class VisionKidsPlaybackControl: IPlaybackControl
{
    override fun getRawFileSuffix(): String
    {
        return (".DNG")
    }

    override fun downloadContentList(callback: IDownloadContentListCallback?) {
        //TODO("Not yet implemented")
    }

    override fun getContentInfo(path: String?, name: String?, callback: IContentInfoCallback?) {
        //TODO("Not yet implemented")
    }

    override fun updateCameraFileInfo(info: ICameraFileInfo?) {
        //TODO("Not yet implemented")
    }

    override fun downloadContentScreennail(
        path: String?,
        callback: IDownloadThumbnailImageCallback?
    ) {
        //TODO("Not yet implemented")
    }

    override fun downloadContentThumbnail(
        path: String?,
        callback: IDownloadThumbnailImageCallback?
    ) {
        //TODO("Not yet implemented")
    }

    override fun downloadContent(
        path: String?,
        isSmallSize: Boolean,
        callback: IDownloadContentCallback?
    ) {
        //TODO("Not yet implemented")
    }

    override fun getCameraContentList(callback: ICameraContentListCallback?) {
        //TODO("Not yet implemented")
    }

    override fun showPictureStarted() {
        //TODO("Not yet implemented")
    }

    override fun showPictureFinished() {
        //TODO("Not yet implemented")
    }

}