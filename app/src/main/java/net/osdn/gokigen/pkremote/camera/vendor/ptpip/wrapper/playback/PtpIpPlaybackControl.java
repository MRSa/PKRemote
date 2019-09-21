package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.app.Activity;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.PtpIpInterfaceProvider;

/**
 *
 *
 */
public class PtpIpPlaybackControl implements IPlaybackControl
{
    //private final String TAG = toString();
    //private final Activity activity;
    //private final PtpIpInterfaceProvider provider;
    //private List<ICameraContent> imageInfo;
    //private SparseArray<PtpIpImageContentInfo> imageContentInfo;
    //private int indexNumber = 0;
    //private ICameraContentListCallback finishedCallback = null;
    private CanonImageObjectReceiver canonImageObjectReceiver;

    public PtpIpPlaybackControl(Activity activity, PtpIpInterfaceProvider provider)
    {
        // this.activity = activity;
        //this.provider = provider;
        canonImageObjectReceiver = new CanonImageObjectReceiver(provider);
        //this.imageContentInfo = new SparseArray<>();
    }

    @Override
    public String getRawFileSuffix()
    {
        return (null);
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
        downloadContentThumbnail(path, callback);
    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback)
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
            Log.v(TAG, "downloadContentThumbnail() : " + path + " " + indexStr);
            int index = Integer.parseInt(indexStr);
            if ((index > 0)&&(index <= imageContentInfo.size()))
            {
                IPtpIpCommandPublisher publisher = provider.getCommandPublisher();
                PtpIpImageContentInfo contentInfo = imageContentInfo.get(index);
                if (!contentInfo.isReceived())
                {
                    publisher.enqueueCommand(new GetImageInfo(index, index, contentInfo));
                }
                publisher.enqueueCommand(new GetThumbNail(index, new PtpIpThumbnailImageReceiver(activity, callback)));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
*/
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
