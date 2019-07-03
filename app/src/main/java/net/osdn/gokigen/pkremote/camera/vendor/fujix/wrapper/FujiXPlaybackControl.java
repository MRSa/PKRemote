package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus;

import static net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.status.IFujiXCameraProperties.IMAGE_FILE_COUNT_STR_ID;

public class FujiXPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();

    private final FujiXInterfaceProvider provider;
    FujiXPlaybackControl(FujiXInterfaceProvider provider)
    {
        this.provider = provider;
    }


    @Override
    public String getRawFileSuffix()
    {
        return (null);
    }

    @Override
    public void downloadContentList(IDownloadContentListCallback callback)
    {

    }

    @Override
    public void getContentInfo(String path, String name, IContentInfoCallback callback)
    {

    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info)
    {

    }

    @Override
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback)
    {

    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback)
    {

    }

    @Override
    public void downloadContent(String path, boolean isSmallSize, IDownloadContentCallback callback)
    {

    }

    @Override
    public void getCameraContentList(final ICameraContentListCallback callback)
    {
        if (callback == null)
        {
            return;
        }
        try
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getCameraContents(callback);
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callback.onErrorOccurred(e);
        }
    }

    private void getCameraContents(ICameraContentListCallback callback)
    {
        try
        {
            ICameraStatus statusListHolder = provider.getCameraStatusListHolder();
            if (statusListHolder != null)
            {
                String count = statusListHolder.getStatus(IMAGE_FILE_COUNT_STR_ID);
                int nofFiles = Integer.parseInt(count);
                Log.v(TAG, "getCameraContents() : " + nofFiles + " (" + count + ")");
            }
            Log.v(TAG, "getCameraContents() : DONE.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
