package net.osdn.gokigen.pkremote.camera.playback;

import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;

import net.osdn.gokigen.pkremote.IInformationReceiver;
import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentsRecognizer;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 *   遠隔カメラのコンテンツを解析・保持するクラス
 *
 *
 */
public class CameraContentsRecognizer implements ICameraContentsRecognizer, ICameraContentListCallback
{
    private final String TAG = toString();
    private final AppCompatActivity activity;
    private final IInformationReceiver informationReceiver;
    private final  IInterfaceProvider interfaceProvider;
    private ICameraContentsListCallback contentsListCallback = null;
    private List<ICameraContent> cameraContentsList = null;

    /**
     *
     *
     */
    public CameraContentsRecognizer(@NonNull AppCompatActivity activity, @NonNull IInterfaceProvider interfaceProvider)
    {
        this.activity = activity;
        this.interfaceProvider = interfaceProvider;
        this.informationReceiver = interfaceProvider.getInformationReceiver();
    }

    /**
     *
     *
     */
    @Override
    public void getRemoteCameraContentsList(ICameraContentsListCallback callback)
    {
        contentsListCallback = callback;
        getRemoteCameraContentsListImpl(this);
    }

    /**
     *
     *
     */
    private void getRemoteCameraContentsListImpl(final ICameraContentListCallback callback)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    IPlaybackControl playbackControl = interfaceProvider.getPlaybackControl();
                    playbackControl.getCameraContentList(callback);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        try
        {
            if (informationReceiver != null)
            {
                informationReceiver.updateMessage(activity.getString(R.string.get_camera_contents_wait), false, false, 0);
            }
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompleted(List<ICameraContent> contentList)
    {
        cameraContentsList = contentList;
        if (contentsListCallback != null)
        {
            try
            {
                // 遠隔のカメラ内のコンテンツ一覧を引っ張ることができたよ、の通知
                contentsListCallback.contentsListCreated(contentList.size());
                if (informationReceiver != null)
                {
                    informationReceiver.updateMessage(activity.getString(R.string.get_camera_contents_finished), false, false, 0);
                }

                // とりあえず、できたコンテンツ一覧をログにダンプしてみる。
                dumpCameraContentList();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onErrorOccurred(Exception e)
    {
        cameraContentsList = null;
        if (informationReceiver != null)
        {
            informationReceiver.updateMessage(activity.getString(R.string.get_camera_contents_error), false, true, Color.RED);
        }

        //  再試行する？ の確認を出す
        e.printStackTrace();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle(activity.getString(R.string.get_camera_contents_error))
                .setMessage(activity.getString(R.string.get_camera_contents_error_retry))
                .setPositiveButton(activity.getString(R.string.dialog_title_button_retry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        getRemoteCameraContentsList(contentsListCallback);
                    }
                });
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                builder.show();
            }
        });
    }

    private void dumpCameraContentList()
    {
        Log.v(TAG, "dumpCameraContentList()");
        if (cameraContentsList == null)
        {
            return;
        }
        try {
            int index = 1;
            for (ICameraContent content : cameraContentsList)
            {
                String cameraId = content.getCameraId();
                String cardId = content.getCardId();
                String path = content.getContentPath();
                String name = content.getContentName();
                Date date = content.getCapturedDate();
                Log.v(TAG, index + " [" + cameraId + "] " + cardId + " : " + path + " " + name + " " + date);
                index++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
