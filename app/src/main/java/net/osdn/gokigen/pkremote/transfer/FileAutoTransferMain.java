package net.osdn.gokigen.pkremote.transfer;

import android.util.Log;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentsRecognizer;
import net.osdn.gokigen.pkremote.playback.detail.MyContentDownloader;
import net.osdn.gokigen.pkremote.scene.IChangeScene;

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
    private final IChangeScene changeScene;
    private final AppCompatActivity activity;
    private final ITransferMessage messageInterface;
    private final MyContentDownloader downloader;
    private boolean firstContent = false;
    private List<ICameraContent> baseContentList = null;
    private List<ICameraContent> currentContentList = null;
    private int dummyCount = 0;

    FileAutoTransferMain(@NonNull AppCompatActivity context, IChangeScene sceneSelector, @NonNull IInterfaceProvider provider, @NonNull ITransferMessage messageInterface)
    {
        this.activity = context;
        this.changeScene = sceneSelector;
        this.interfaceProvider = provider;
        this.messageInterface = messageInterface;
        this.downloader = new MyContentDownloader(context, provider.getPlaybackControl());
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
            dummyCount = 0;
            baseContentList = null;
            currentContentList = null;
            firstContent = true;

            // 現在のカメラ画像一覧をとってくる
            interfaceProvider.getPlaybackControl().getCameraContentList(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   画像の自動転送  本処理
     *
     */
    void downloadFiles()
    {
        try
        {
            Log.v(TAG, "CHECK FILE");
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.gc();
    }

    private boolean downloadImages()
    {
        try
        {
            Log.v(TAG, "downloadImages()");

            // baseContentList と currentContentList の差分を確認する

            // 見つけた画像を(連続して)ダウンロードする

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
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
                dummyCount++;
                if (baseSize != currentSize)
                {
                    // 画像ファイル数が変わった！
                    messageInterface.showInformation(activity.getString(R.string.image_checking) + " " + currentSize);
                    if (downloadImages())
                    {
                        // ベースのコンテンツリストを更新する
                        baseContentList = currentContentList;
                        currentContentList = null;
                    }
                }
                else
                {
                    // 画像ファイルサイズが変わっていない場合は表示を消す
                    messageInterface.showInformation("[" + dummyCount + "]");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ICameraContentListCallback
    @Override
    public void onErrorOccurred(Exception e)
    {
        Log.v(TAG, "RECEIVE FAILURE...");
        e.printStackTrace();
    }
}
