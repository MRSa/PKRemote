package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.app.Activity;
import android.content.SharedPreferences;

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
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonRequestInnerDevelopEnd;
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
    private String raw_suffix = "CR2";
    private CanonImageObjectReceiver canonImageObjectReceiver;

    public PtpIpPlaybackControl(Activity activity, PtpIpInterfaceProvider provider)
    {
        this.activity = activity;
        this.provider = provider;
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
        // Thumbnail と同じ画像を表示する
        //downloadContentThumbnail(path, callback);
/**/
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
                int storageId = content.getStorageId();
                int objectId = content.getId();
                // Log.v(TAG, "downloadContentThumbnail() " + indexStr + " [" + objectId + "] (" + storageId + ")");

                // 一連の画像取得シーケンス(RequestInnerDevelopStart, GetPartialObject, TransferComplete, RequestInnerDevelopEnd )を送信キューに積み込む
                PtpIpScreennailImageReceiver receiver = new PtpIpScreennailImageReceiver(activity, objectId, publisher, callback);
                publisher.enqueueCommand(new CanonRequestInnerDevelopStart(receiver, true, objectId, objectId));                                                                         // 0x9141 : RequestInnerDevelopStart
                publisher.enqueueCommand(new PtpIpCommandGeneric(receiver, true, (objectId + 1), 0x9107, 12, 0x01, 0x00, 0x00200000));    // 0x9107 : GetPartialObject  (元は 0x00020000)
                publisher.enqueueCommand(new PtpIpCommandGeneric(receiver, true, (objectId + 2), 0x9117, 4,0x01));                                          // 0x9117 : TransferComplete
                publisher.enqueueCommand(new CanonRequestInnerDevelopEnd(receiver, true, (objectId + 3)));                                                                               // 0x9143 : RequestInnerDevelopEnd
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
/**/
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
                int storageId = content.getStorageId();
                int objectId = content.getId();
                // Log.v(TAG, "downloadContentThumbnail() " + indexStr + " [" + objectId + "] (" + storageId + ")");
                publisher.enqueueCommand(new PtpIpCommandGeneric(new PtpIpThumbnailImageReceiver(activity, callback), false, objectId, 0x910a, 8, objectId, 0x00032000));
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
/*
        try
        {
            int start = 0;
            if (path.indexOf("/") == 0)
            {
                start = 1;
            }
            String indexStr = path.substring(start, path.indexOf("."));
            Log.v(TAG, "FujiX::downloadContent() : " + path + " " + indexStr);
            int index = Integer.parseInt(indexStr);
            //PtpIpImageContentInfo contentInfo = imageContentInfo.get(index);   // 特にデータを更新しないから大丈夫か？
            if ((index > 0)&&(index <= imageContentInfo.size()))
            {
                IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
                publisher.enqueueCommand(new GetFullImage(index, new PtpIpFullImageReceiver(callback)));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
*/
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

/*
    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        // イメージ数の一覧が取得できなかった場合にここで作る。
        if (rx_body.length < 16)
        {
            // インデックスデータがなくなったことを検出...データがそろったとして応答する。
            //Log.v(TAG, "IMAGE LIST : " + imageContentInfo.size());
            finishedCallback.onCompleted(getCameraContentList());
            finishedCallback = null;
            return;
        }
        try
        {
            Log.v(TAG, "RECEIVED IMAGE INFO : " + indexNumber);

            // 受信データを保管しておく
            imageContentInfo.append(indexNumber, new PtpIpImageContentInfo(indexNumber, rx_body));

            // 次のインデックスの情報を要求する
            indexNumber++;
            IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
            publisher.enqueueCommand(new GetImageInfo(indexNumber, indexNumber, this));
        }
        catch (Exception e)
        {
            // エラーになったら、そこで終了にする
            e.printStackTrace();
            finishedCallback.onCompleted(getCameraContentList());
            finishedCallback = null;
        }
    }
*/

/*
    private List<ICameraContent> getCameraContentList()
    {
        /// ダサいけど...コンテナクラスを詰め替えて応答する
        List<ICameraContent> contentList = new ArrayList<>();
        int listSize = imageContentInfo.size();
        for(int index = 0; index < listSize; index++)
        {
            contentList.add(imageContentInfo.valueAt(index));
        }
        return (contentList);
    }
*/

}
