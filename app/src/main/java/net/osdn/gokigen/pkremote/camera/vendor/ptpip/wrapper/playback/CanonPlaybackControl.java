package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.IInformationReceiver;
import net.osdn.gokigen.pkremote.R;
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
public class CanonPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private final AppCompatActivity activity;
    private final PtpIpInterfaceProvider provider;
    private final CanonFullImageReceiver fullImageReceiver;
    private final ICanonImageReceiver smallImageReciever;
    private String raw_suffix = "CR2";
    private boolean useScreennailImage = false;
    private final CanonImageObjectReceiver canonImageObjectReceiver;
    private int smallImageSequence = 0;

    public CanonPlaybackControl(AppCompatActivity activity, PtpIpInterfaceProvider provider)
    {
        int delayMs = 20;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            raw_suffix = preferences.getString(IPreferencePropertyAccessor.CANON_RAW_SUFFIX, IPreferencePropertyAccessor.CANON_RAW_SUFFIX_DEFAULT_VALUE);
            useScreennailImage = preferences.getBoolean(IPreferencePropertyAccessor.CANON_USE_SCREENNAIL_AS_SMALL, false);
            try
            {
                delayMs = Integer.parseInt(preferences.getString(IPreferencePropertyAccessor.CANON_RECEIVE_WAIT, IPreferencePropertyAccessor.CANON_RECEIVE_WAIT_DEFAULT_VALUE));
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
            }
            if (delayMs < 10)
            {
                delayMs = 10;  // 最短は 10msにする
            }
            try
            {
                smallImageSequence = Integer.parseInt(preferences.getString(IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE, IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE_DEFAULT_VALUE));
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        this.activity = activity;
        this.provider = provider;
        this.fullImageReceiver = new CanonFullImageReceiver(activity, provider.getCommandPublisher());
        if (smallImageSequence == 2)
        {
            this.smallImageReciever = new CanonFullImageReceiver(activity, provider.getCommandPublisher());
        }
        else if (smallImageSequence == 3)
        {
            this.smallImageReciever = new CanonImageReceiver(activity, provider.getCommandPublisher(), smallImageSequence);
        }
        else
        {
            this.smallImageReciever = new CanonImageReceiver(activity, provider.getCommandPublisher(), smallImageSequence);
        }
        canonImageObjectReceiver = new CanonImageObjectReceiver(provider, delayMs);
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

        if (!useScreennailImage)
        {
            // Thumbnail と同じ画像を表示する
            downloadContentThumbnail(path, callback);
            return;
        }

        try
        {
            int start = 0;
            if (path.indexOf("/") == 0)
            {
                start = 1;
            }
            final String indexStr = path.substring(start);
            CanonImageContentInfo content = canonImageObjectReceiver.getContentObject(indexStr);
            if (content != null)
            {
                IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
                //int storageId = content.getStorageId();
                int objectId = content.getId();

                // 画像表示中...のメッセージを表示する
                IInformationReceiver display = provider.getInformationReceiver();
                if (display != null)
                {
                    String message = activity.getString(R.string.canon_get_image_screennail);
                    display.updateMessage(message, false, true, Color.LTGRAY);
                }

                // 画像を取得する
                CanonScreennailImageReceiver receiver = new CanonScreennailImageReceiver(activity, objectId, publisher, callback);
                if (smallImageSequence == 1)
                {
                    publisher.enqueueCommand(new CanonRequestInnerDevelopStart(receiver, objectId, true, objectId, objectId, 0x06, 0x02));   // 0x9141 : RequestInnerDevelopStart
                }
                else
                {
                    publisher.enqueueCommand(new CanonRequestInnerDevelopStart(receiver, objectId, true, objectId, objectId, 0x0f, 0x02));   // 0x9141 : RequestInnerDevelopStart
                }
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

            final String indexStr = path.substring(start);
            CanonImageContentInfo content = canonImageObjectReceiver.getContentObject(indexStr);
            if (content != null)
            {
                int objectId = content.getId();
                IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
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
            CanonImageContentInfo content = canonImageObjectReceiver.getContentObject(indexStr);
            if (content != null)
            {
                if (isSmallSize)
                {
                    // スモールサイズの画像取得コマンド（シーケンス）を発行する
                    smallImageReciever.issueCommand(content.getId(), content.getOriginalSize(), callback);
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
            Thread thread = new Thread(() -> canonImageObjectReceiver.getCameraContents(callback));
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
