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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
    private boolean isLoadedContents = false;

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
    public void getRemoteCameraContentsList(boolean isReload, ICameraContentsListCallback callback)
    {
        contentsListCallback = callback;
        if ((isLoadedContents)&&(cameraContentsList != null)&&(!isReload))
        {
            Log.v(TAG, "getRemoteCameraContentsList() : cached data.");
            if (callback != null)
            {
                callback.contentsListCreated(cameraContentsList.size());
            }
            return;
        }
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
                    isLoadedContents = false;
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
        isLoadedContents = true;
        if (contentsListCallback != null)
        {
            try
            {
                // 遠隔のカメラ内のコンテンツ一覧を引っ張ることができたよ、の通知
                contentsListCallback.contentsListCreated(contentList.size());
                if (informationReceiver != null)
                {
                    String message = activity.getString(R.string.get_camera_contents_finished) + " : " + contentList.size();
                    informationReceiver.updateMessage(message, false, false, 0);
                }

                // 最新の撮影データから並べる
                Collections.sort(contentList, new Comparator<ICameraContent>() {
                    @Override
                    public int compare(ICameraContent lhs, ICameraContent rhs)
                    {
                        long diff = rhs.getCapturedDate().getTime() - lhs.getCapturedDate().getTime();
                        if (diff == 0)
                        {
                            diff = rhs.getContentName().compareTo(lhs.getContentName());
                        }
                        return (int)Math.min(Math.max(-1, diff), 1);
                    }
                });

                //// とりあえず、できたコンテンツ一覧をログにダンプしてみる。
                // dumpCameraContentList();
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
        isLoadedContents = false;
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
                        getRemoteCameraContentsList(true, contentsListCallback);
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

/*
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
*/

    /**
     *    ファイル全件のリストを返す
     *
     *
     */
    @Override
    public List<ICameraContent> getContentsList()
    {
        if (cameraContentsList == null)
        {
            getRemoteCameraContentsListImpl(this);
            return (new ArrayList<>());
        }
        return (cameraContentsList);
    }

    /**
     *　 指定された年月日（yyyy/MM/DD）に含まれている一覧を応答する
     *
     *
     */
    @Override
    public List<ICameraContent> getContentsListAtDate(String date)
    {
        Log.v(TAG, "getContentsListAtDate() : " + date);
        if (date.equals("ALL"))
        {
            return (getContentsList());
        }
        if (cameraContentsList == null)
        {
            getRemoteCameraContentsListImpl(this);
            return (new ArrayList<>());
        }

        ArrayList<ICameraContent> targetList = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        for (ICameraContent content : cameraContentsList)
        {
            String capturedDate = format.format(content.getCapturedDate());
            if (date.equals(capturedDate))
            {
                targetList.add(content);
            }
        }
        Collections.sort(targetList, new Comparator<ICameraContent>() {
            @Override
            public int compare(ICameraContent lhs, ICameraContent rhs)
            {
                long diff = rhs.getCapturedDate().getTime() - lhs.getCapturedDate().getTime();
                if (diff == 0)
                {
                    diff = rhs.getContentName().compareTo(lhs.getContentName());
                }
                return (int)Math.min(Math.max(-1, diff), 1);
            }
        });
        //Collections.sort(targetList);
        return (targetList);
    }

    /**
     *   指定されたパスに入っている一覧を応答する
     *
     *
     */
    @Override
    public List<ICameraContent> getContentsListAtPath(String path)
    {
        Log.v(TAG, "getContentsListAtPath() : " + path);
        if (path.equals("ALL"))
        {
            // 全件の場合...
            return (getContentsList());
        }
        if (cameraContentsList == null)
        {
            getRemoteCameraContentsListImpl(this);
            return (new ArrayList<>());
        }

        ArrayList<ICameraContent> targetList = new ArrayList<>();
        for (ICameraContent content : cameraContentsList)
        {
            if (path.equals(content.getContentPath()))
            {
                targetList.add(content);
            }
        }
        Collections.sort(targetList, new Comparator<ICameraContent>() {
            @Override
            public int compare(ICameraContent lhs, ICameraContent rhs)
            {
                long diff = rhs.getCapturedDate().getTime() - lhs.getCapturedDate().getTime();
                if (diff == 0)
                {
                    diff = rhs.getContentName().compareTo(lhs.getContentName());
                }
                return (int)Math.min(Math.max(-1, diff), 1);
            }
        });
        //Collections.sort(targetList);
        //Log.v(TAG, "getContentsListAtPath() " + targetList.size());
        return (targetList);
    }

    /**
     *   撮影年月日の一覧を取得する
     *   （最新の撮影日からの並びに整列する）
     *
     */
    @Override
    public List<String> getDateList()
    {
        if (cameraContentsList == null)
        {
            return (new ArrayList<>());
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        HashMap<String, String> map = new HashMap<>();
        for (ICameraContent content : cameraContentsList)
        {
            map.put(format.format(content.getCapturedDate()), content.getContentName());
        }
        ArrayList<String> dateList = new ArrayList<>(map.keySet());
        Collections.sort(dateList, Collections.reverseOrder());
        return (dateList);
    }

    /**
     *   ファイルパス（ディレクトリ）の一覧を取得する
     *   たぶん、新しい画像が入ったディレクトリからのリスト
     *
     */
    @Override
    public List<String> getPathList()
    {
        if (cameraContentsList == null)
        {
            return (new ArrayList<>());
        }
        HashMap<String, String> map = new HashMap<>();
        for (ICameraContent content : cameraContentsList)
        {
            map.put(content.getContentPath(), content.getContentName());
        }
        ArrayList<String> pathList = new ArrayList<>(map.keySet());
        Collections.sort(pathList, Collections.reverseOrder());
        return (pathList);
    }
}
