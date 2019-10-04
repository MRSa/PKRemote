package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.PtpIpInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonRequestInnerDevelopStart;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

/**
 *
 *
 */
public class PtpIpPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private final Activity activity;
    private final PtpIpInterfaceProvider provider;
    private final PtpIpFullImageReceiver fullImageReceiver;
    private final PtpIpSmallImageReceiver smallImageReciever;
    private String raw_suffix = "CR2";
    private CanonImageObjectReceiver canonImageObjectReceiver;

    public PtpIpPlaybackControl(Activity activity, PtpIpInterfaceProvider provider)
    {
        this.activity = activity;
        this.provider = provider;
        this.fullImageReceiver = new PtpIpFullImageReceiver(activity, provider.getCommandPublisher());
        this.smallImageReciever = new PtpIpSmallImageReceiver(activity, provider.getCommandPublisher());
        canonImageObjectReceiver = new CanonImageObjectReceiver(provider);

        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            raw_suffix = preferences.getString(IPreferencePropertyAccessor.CANON_RAW_SUFFIX, IPreferencePropertyAccessor.CANON_RAW_SUFFIX_DEFAULT_VALUE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String getRawFileSuffix()
    {
        return (raw_suffix);
    }

    @Override
    public void downloadContentList(IDownloadContentListCallback callback)
    {
        // なにもしない。(未使用)
    }

    @Override
    public void getContentInfo(String path, String name, IContentInfoCallback callback)
    {
        // showFileInformation

    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info)
    {
        //  なにもしない
    }

    @Override
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback)
    {
        Log.v(TAG, " downloadContentScreennail() " + path);

        // Thumbnail と同じ画像を表示する
        //downloadContentThumbnail(path, callback);

        try
        {
            int start = 0;
            if (path.indexOf("/") == 0)
            {
                start = 1;
            }
            final String indexStr = path.substring(start);
            PtpIpImageContentInfo content = canonImageObjectReceiver.getContentObject(indexStr);
            if (content != null)
            {
                IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
                //int storageId = content.getStorageId();
                int objectId = content.getId();
                // Log.v(TAG, "downloadContentThumbnail() " + indexStr + " [" + objectId + "] (" + storageId + ")");

                // 画像を取得する
                PtpIpScreennailImageReceiver receiver = new PtpIpScreennailImageReceiver(activity, objectId, publisher, callback);
                publisher.enqueueCommand(new CanonRequestInnerDevelopStart(receiver, objectId, true, objectId, objectId));   // 0x9141 : RequestInnerDevelopStart
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContentThumbnail(String path, final IDownloadThumbnailImageCallback callback)
    {
        try
        {
            int start = 0;
            if (path.indexOf("/") == 0)
            {
                start = 1;
            }
            //String indexStr = path.substring(start, path.indexOf("."));
            final String indexStr = path.substring(start);
            //Log.v(TAG, "downloadContentThumbnail() : [" + path + "] " + indexStr);

            PtpIpImageContentInfo content = canonImageObjectReceiver.getContentObject(indexStr);
            if (content != null)
            {
                IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
                //int storageId = content.getStorageId();
                int objectId = content.getId();
                // Log.v(TAG, "downloadContentThumbnail() " + indexStr + " [" + objectId + "] (" + storageId + ")");
                publisher.enqueueCommand(new PtpIpCommandGeneric(new PtpIpThumbnailImageReceiver(activity, callback), objectId, false, 0, 0x910a, 8, objectId, 0x00032000));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContent(String path, boolean isSmallSize, IDownloadContentCallback callback)
    {
        try
        {
            int start = 0;
            if (path.indexOf("/") == 0)
            {
                start = 1;
            }
            final String indexStr = path.substring(start);
            PtpIpImageContentInfo content = canonImageObjectReceiver.getContentObject(indexStr);
            if (content != null)
            {
                if (isSmallSize)
                {
                    // スモールサイズの画像取得コマンド（シーケンス）を発行する
                    smallImageReciever.issueCommand(content.getId(), callback);
                }
                else
                {
                    // オリジナル画像の取得コマンド（シーケンス）を発行する
                    fullImageReceiver.issueCommand(content.getId(), content.getOriginalSize(), callback);
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
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
                    canonImageObjectReceiver.getCameraContents(callback);
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

    @Override
    public void showPictureStarted()
    {
        try
        {
            Log.v(TAG, "   showPictureStarted() ");

            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            publisher.flushHoldQueue();
            System.gc();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void showPictureFinished()
    {
        try
        {
            Log.v(TAG, "   showPictureFinished() ");

            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            publisher.flushHoldQueue();
            System.gc();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
