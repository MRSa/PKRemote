package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper;

import android.util.Log;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.GetImageInfo;

import java.util.ArrayList;
import java.util.List;

import static net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.status.IFujiXCameraProperties.IMAGE_FILE_COUNT_STR_ID;

public class FujiXPlaybackControl implements IPlaybackControl, IFujiXCommandCallback
{
    private final String TAG = toString();
    private final FujiXInterfaceProvider provider;
    private List<ICameraContent> imageInfo;
    private int indexNumber = 0;
    private ICameraContentListCallback finishedCallback = null;

    FujiXPlaybackControl(FujiXInterfaceProvider provider)
    {
        this.provider = provider;
        this.imageInfo = new ArrayList<>();
    }


    @Override
    public String getRawFileSuffix() {
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
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback) {

    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback) {

    }

    @Override
    public void downloadContent(String path, boolean isSmallSize, IDownloadContentCallback callback) {

    }

    @Override
    public void getCameraContentList(final ICameraContentListCallback callback) {
        if (callback == null) {
            return;
        }
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getCameraContents(callback);
                }
            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
            callback.onErrorOccurred(e);
        }
    }

    private void getCameraContents(ICameraContentListCallback callback)
    {
        int nofFiles = -1;
        try {
            finishedCallback = callback;
            ICameraStatus statusListHolder = provider.getCameraStatusListHolder();
            if (statusListHolder != null) {
                String count = statusListHolder.getStatus(IMAGE_FILE_COUNT_STR_ID);
                nofFiles = Integer.parseInt(count);
                Log.v(TAG, "getCameraContents() : " + nofFiles + " (" + count + ")");
            }
            Log.v(TAG, "getCameraContents() : DONE.");
            if (nofFiles > 0)
            {
                // 件数ベースで取得する(情報は、後追いで反映させる...この方式だと、キューに積みまくってるが、、、)
                checkImageFiles(nofFiles);
            }
            else
            {
                checkImageFileAll();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            finishedCallback.onErrorOccurred(e);
            finishedCallback = null;
        }
    }

    /**
     *   最初から取得可能なイメージ情報を(件数ベースで)取得する
     *
     */
    private void checkImageFiles(int nofFiles)
    {
        try
        {
            imageInfo.clear();
            IFujiXCommandPublisher publisher = provider.getCommandPublisher();
            for (int index = 1; index <= nofFiles; index++)
            {
                FujiXImageContentInfo info = new FujiXImageContentInfo(index, null);
                publisher.enqueueCommand(new GetImageInfo(index, index, info));
                imageInfo.add(info);
            }

            // インデックスデータがなくなったことを検出...データがそろったとして応答する。
            Log.v(TAG, "IMAGE LIST : " + imageInfo.size() + " (" + nofFiles + ")");
            finishedCallback.onCompleted(imageInfo);
            finishedCallback = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   最初から取得可能なイメージ情報をすべて取得する
     *
     */
    private void checkImageFileAll()
    {
        try
        {
            imageInfo.clear();
            indexNumber = 1;
            IFujiXCommandPublisher publisher = provider.getCommandPublisher();
            publisher.enqueueCommand(new GetImageInfo(indexNumber, indexNumber, this));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        // イメージ数の一覧が取得できなかった場合にここで作る。
        if (rx_body.length < 16)
        {
            // インデックスデータがなくなったことを検出...データがそろったとして応答する。
            Log.v(TAG, "IMAGE LIST : " + imageInfo.size());
            finishedCallback.onCompleted(imageInfo);
            finishedCallback = null;
            return;
        }
        try
        {
            Log.v(TAG, "RECEIVED IMAGE INFO : " + indexNumber);

            // 受信データを保管しておく
            imageInfo.add(new FujiXImageContentInfo(indexNumber, rx_body));

            // 次のインデックスの情報を要求する
            indexNumber++;
            IFujiXCommandPublisher publisher = provider.getCommandPublisher();
            publisher.enqueueCommand(new GetImageInfo(indexNumber, indexNumber, this));
        }
        catch (Exception e)
        {
            // エラーになったら、そこで終了にする
            e.printStackTrace();
            finishedCallback.onCompleted(imageInfo);
            finishedCallback = null;
        }
    }
}
