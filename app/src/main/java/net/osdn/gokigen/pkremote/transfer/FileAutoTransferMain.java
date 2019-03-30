package net.osdn.gokigen.pkremote.transfer;

import android.util.Log;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.playback.detail.MyContentDownloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 *   画像の自動転送を実現するクラス
 *   (UIスレッドでは動いていないので注意)
 */
class FileAutoTransferMain implements ICameraContentListCallback
{
    private final String TAG = this.toString();

    private final IInterfaceProvider interfaceProvider;
    private final AppCompatActivity activity;
    private final ITransferMessage messageInterface;
    private final MyContentDownloader downloader;
    private boolean firstContent = false;
    private List<ICameraContent> baseContentList = null;
    private List<ICameraContent> currentContentList = null;
    private HashMap<String, ICameraContent> contentHashMap;
    private boolean getRaw = false;
    private boolean smallSize = false;
    private boolean isChecking = false;


    FileAutoTransferMain(@NonNull AppCompatActivity context, @NonNull IInterfaceProvider provider, @NonNull ITransferMessage messageInterface)
    {
        this.activity = context;
        this.interfaceProvider = provider;
        this.messageInterface = messageInterface;
        this.downloader = new MyContentDownloader(context, provider.getPlaybackControl());
        this.contentHashMap = new HashMap<>();
    }

    /**
     *   画像の自動転送 前処理
     *
     */
    void start(boolean getRaw, boolean smallSize)
    {
        String message = "TRANSFER START [raw:" + getRaw + "] [small:" + smallSize + "]";
        Log.v(TAG, message);

        try
        {
            // 内部データの初期化
            baseContentList = null;
            currentContentList = null;
            firstContent = true;
            contentHashMap.clear();

            this.getRaw = getRaw;
            this.smallSize = smallSize;

            // RunモードをRecordingに変更する。
            //  (Olympus Air向けだったのだが ... でも liveview が開始されていないと撮影できなさそう...)
            interfaceProvider.getCameraRunMode().changeRunMode(true);

            // 現在のカメラ画像一覧をとってくる
            interfaceProvider.getPlaybackControl().getCameraContentList(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    boolean isChecking()
    {
        return (isChecking);
    }

    /**
     *   画像の自動転送  本処理
     *
     */
    void checkFiles()
    {
        try
        {
            Log.v(TAG, "CHECK FILE");
            isChecking = true;
            interfaceProvider.getPlaybackControl().getCameraContentList(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   画像の自動転送 後処理
     *
     */
    void finish()
    {
        try
        {
            Log.v(TAG, "FINISH");
            messageInterface.showInformation("");
            baseContentList = null;
            currentContentList = null;

            // RunモードをPlaybackモードに戻す。
            interfaceProvider.getCameraRunMode().changeRunMode(false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.gc();
    }

    private boolean downloadImages()
    {
        Log.v(TAG, "downloadImages()");
        boolean isDownload = false;
        try
        {
            ArrayList<ICameraContent> addContent = new ArrayList<>();
            for (ICameraContent content : currentContentList)
            {
                String key = (content.getContentPath() + "/" + content.getContentName()).toLowerCase();
                //Log.v(TAG, "KEY : " + key);

                // 追加ファイル発見！
                if (!contentHashMap.containsKey(key))
                {
                    Log.v(TAG, "FILE(add) : " + key);
                    contentHashMap.put(key, content);
                    if ((key.endsWith(".jpg"))||(getRaw))
                    {
                        addContent.add(content);
                    }
                }
            }

            // 見つけた画像を(連続して)ダウンロードする (ここから)
            messageInterface.showInformation(activity.getString(R.string.add_image_pics) + " " + addContent.size());
            if (addContent.size() > 0)
            {
                //  一括ダウンロードする
                startDownloadBatch(addContent, smallSize);
                isDownload = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (isDownload);
    }

    /**
     *    一括ダウンロードの開始
     *
     * @param isSmall  小さいサイズ(JPEG)
     */
    private void startDownloadBatch(final ArrayList<ICameraContent> imageContentList, final boolean isSmall)
    {
        try
        {
            int count = 1;
            int totalSize = imageContentList.size();
            for (ICameraContent content : imageContentList)
            {
                downloader.startDownload(content, " (" + count + "/" + totalSize + ") ", null, isSmall);
                do
                {
                    try
                    {
                        // ここでダウンロードが終わるまで、すこし待つ
                        Thread.sleep(300);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                } while (downloader.isDownloading());
                count++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ICameraContentListCallback
    @Override
    public void onCompleted(List<ICameraContent> contentList)
    {
        Log.v(TAG, "RECEIVE CONTENT LIST");
        try
        {
            if (firstContent)
            {
                baseContentList = contentList;
                if ((baseContentList != null)&&(baseContentList.size() > 0))
                {
                    firstContent = false;

                    //  初期データを突っ込んでおく...
                    for (ICameraContent content : baseContentList)
                    {
                        String key = (content.getContentPath() + "/" + content.getContentName()).toLowerCase();
                        Log.v(TAG, "FILE : " + key);
                        contentHashMap.put(key, content);
                    }
                }
            }
            else
            {
                currentContentList = contentList;
            }
            if ((baseContentList != null)&&(currentContentList != null)&&(currentContentList.size() > 0))
            {
                // コンテンツ数の差異を確認する。
                int baseSize = baseContentList.size();
                int currentSize = currentContentList.size();
                if (baseSize != currentSize)
                {
                    // 画像ファイル数が変わった！
                    messageInterface.showInformation(activity.getString(R.string.image_checking) + " " + currentSize);

                    // 画像のダウンロードを実行する
                    if (downloadImages())
                    {
                        // 実行がうまくいった場合は表示を更新する
                        messageInterface.showInformation(activity.getString(R.string.image_download_done));
                    }

                    ////////////////////////////////////////  現在のカメラ内画像情報を差し替えて、次の増加分にそなえる
                    baseContentList = currentContentList;
                    currentContentList = null;
                }
                else
                {
                    // 画像ファイル数が変わっていない場合は表示を消す
                    messageInterface.showInformation(" ");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        isChecking = false;
    }

    // ICameraContentListCallback
    @Override
    public void onErrorOccurred(Exception e)
    {
        Log.v(TAG, "RECEIVE FAILURE...");
        e.printStackTrace();
        isChecking = false;
    }
}
