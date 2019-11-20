package net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.playback;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

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
import net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.NikonInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback.PtpIpThumbnailImageReceiver;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

/**
 *
 *
 */
public class NikonPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private final Activity activity;
    private final NikonInterfaceProvider provider;
    private final NikonFullImageReceiver fullImageReceiver;
    private final NikonSmallImageReceiver smallImageReciever;
    private String raw_suffix = "NEF";
    private boolean use_screennail_image = false;
    private NikonImageObjectReceiver nikonImageObjectReceiver;

    public NikonPlaybackControl(Activity activity, NikonInterfaceProvider provider)
    {
        this.activity = activity;
        this.provider = provider;
        nikonImageObjectReceiver = new NikonImageObjectReceiver(provider);
        this.fullImageReceiver = new NikonFullImageReceiver(activity, provider.getCommandPublisher());
        this.smallImageReciever = new NikonSmallImageReceiver(activity, provider.getCommandPublisher());

        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            raw_suffix = preferences.getString(IPreferencePropertyAccessor.CANON_RAW_SUFFIX, IPreferencePropertyAccessor.CANON_RAW_SUFFIX_DEFAULT_VALUE);
            use_screennail_image = preferences.getBoolean(IPreferencePropertyAccessor.NIKON_USE_SCREENNAIL_AS_SMALL, false);
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
        if (!use_screennail_image)
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
            NikonImageContentInfo content = nikonImageObjectReceiver.getContentObject(indexStr);
            if (content != null)
            {
                IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
                int objectId = content.getObjectId();

                // 画像表示中...のメッセージを表示する
                IInformationReceiver display = provider.getInformationReceiver();
                if (display != null)
                {
                    String message = activity.getString(R.string.canon_get_image_screennail);
                    display.updateMessage(message, false, true, Color.LTGRAY);
                }

                // 画像を取得する
                publisher.enqueueCommand(new PtpIpCommandGeneric(new NikonScreennailImageReceiver(activity, callback), objectId, false, 0, 0x90c4, 4, objectId));
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
            NikonImageContentInfo content = nikonImageObjectReceiver.getContentObject(indexStr);
            if (content != null)
            {
                IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
                final int objectId = content.getObjectId();
                if (!content.isDateValid())
                {
                    publisher.enqueueCommand(new PtpIpCommandGeneric(new IPtpIpCommandCallback() {
                        @Override
                        public void receivedMessage(int id, byte[] rx_body)
                        {
                            updateImageContent(objectId, rx_body);
                        }

                        @Override
                        public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body) {

                        }

                        @Override
                        public boolean isReceiveMulti() {
                            return (false);
                        }
                    }, objectId, false, 0, 0x1008, 4, objectId));  // getObjectInfo
                }

                // Log.v(TAG, "downloadContentThumbnail() " + indexStr + " [" + objectId + "] (" + storageId + ")");
                publisher.enqueueCommand(new PtpIpCommandGeneric(new PtpIpThumbnailImageReceiver(activity, callback), objectId, false, 0, 0x100a, 4, objectId));  // getThumb
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void updateImageContent(int objectId, byte[] rx_body)
    {
        try
        {
            NikonImageContentInfo content = nikonImageObjectReceiver.getImageContent(objectId);
            // 受信データを解析してオブジェクトに値をまとめて設定する
            int readPosition = 40;
            int imageSize =  ((int) rx_body[readPosition] & 0x000000ff) +
                    (((int) rx_body[readPosition + 1] & 0x000000ff) << 8) +
                    (((int) rx_body[readPosition + 2] & 0x000000ff) << 16) +
                    (((int) rx_body[readPosition + 3] & 0x000000ff) << 24);
            content.setOriginalSize(imageSize);
            Log.v(TAG, " CONTENT SIZE : " + imageSize);
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
            NikonImageContentInfo content = nikonImageObjectReceiver.getContentObject(indexStr);
            if (content != null)
            {
                if (isSmallSize)
                {
                    // スモールサイズの画像取得コマンド（シーケンス）を発行する
                    smallImageReciever.issueCommand(content.getObjectId(), callback);
                }
                else
                {
                    // オリジナル画像の取得コマンド（シーケンス）を発行する
                    fullImageReceiver.issueCommand(content.getObjectId(), content.getOriginalSize(), callback);
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
                    nikonImageObjectReceiver.getCameraContents(callback);
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
