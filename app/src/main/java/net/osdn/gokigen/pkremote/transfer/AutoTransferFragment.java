package net.osdn.gokigen.pkremote.transfer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.playback.IContentDownloadNotify;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 *   自動転送クラス
 *
 */
public class AutoTransferFragment extends Fragment implements View.OnClickListener, ITransferMessage, IContentDownloadNotify
{
    private final String TAG = this.toString();

    private static final int SLEEP_MS = 300;         // 動作チェック待ち時間
    private static final int SLEEP_WAIT_MS = 4500;  // 一覧確認の待機時間 (4.5秒おき)

    private AppCompatActivity activity = null;
    private FileAutoTransferMain transferMain = null;
    private View myView = null;
    private boolean transferThreadIsRunning = false;
    private boolean startTransferReceived = false;

    public static AutoTransferFragment newInstance(@NonNull AppCompatActivity context, @NonNull IInterfaceProvider provider)
    {
        AutoTransferFragment instance = new AutoTransferFragment();
        instance.prepare(context, provider);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        //arguments.putString("title", title);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    /**
     *
     */
    private void prepare(@NonNull AppCompatActivity activity, @NonNull IInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, "prepare()");
        this.activity = activity;
        transferMain = new FileAutoTransferMain(activity, interfaceProvider, this, this);
    }

    /**
     *
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
    }

    /**
     *
     *
     */
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        Log.v(TAG, "onAttach()");
    }

    /**
     *
     *
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        Log.v(TAG, "onCreateView()");
        if (myView != null)
        {
            // Viewを再利用。。。
            Log.v(TAG, "onCreateView() : called again, so do nothing... : " + myView);
            return (myView);
        }

        myView = inflater.inflate(R.layout.fragment_auto_transfer, container, false);
        try
        {
            prepare(myView);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (myView);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        controlButton(true);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        // 画面を抜ける時に転送モードであった場合は、自動転送を停止させる
        if (transferThreadIsRunning)
        {
            finishTransfer();
        }
    }

    private void prepare(@NonNull View view)
    {
        try
        {
            Button start = view.findViewById(R.id.transfer_start_button);
            if (start != null)
            {
                start.setOnClickListener(this);
            }

            Button stop = view.findViewById(R.id.transfer_stop_button);
            if (stop != null)
            {
                stop.setOnClickListener(this);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   転送開始
     *
     */
    private void startTransfer()
    {
        if (activity == null)
        {
            //  activityがない場合は動かない。
            Log.v(TAG, "ACTIVITY IS NULL...");
            return;
        }
        try
        {
            // とにかく転送スレッドを止める指示を出す
            transferThreadIsRunning = false;
            while (startTransferReceived)
            {
                // すでにコマンドは発行状態...終わるまで待つ
                Thread.sleep(SLEEP_MS);
            }

            // STARTボタンを無効化してぶるぶるする...
            controlButton(false);
            Vibrator vibrator = (activity != null) ? (Vibrator) activity.getSystemService(VIBRATOR_SERVICE) : null;
            if (vibrator != null)
            {
                vibrator.vibrate(50);
            }
            startTransferReceived = true;

            // 画像を初期データにする
            ImageView imageView = activity.findViewById(R.id.image_view_area);
            imageView.setImageResource(R.drawable.ic_satellite_grey_24dp);

            // 画面上にある自動転送の設定を取得
            CheckBox raw = activity.findViewById(R.id.check_auto_download_raw);
            CheckBox original = activity.findViewById(R.id.check_auto_download_original);
            final boolean isRaw = raw.isChecked();
            final boolean isSmallSize = !original.isChecked();  // 画面上のチェックとは逆にする...

            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    //int count = 0;
                    if (transferMain != null)
                    {
                        // 前処理...
                        transferMain.start(isRaw, isSmallSize);
                    }
                    while (transferThreadIsRunning)
                    {
                        try
                        {
                            if (transferMain != null)
                            {
                                //// 現在時刻を取得する
                                //long checkStartTime = System.currentTimeMillis();

                                // チェックして追加ファイルがあったらダウンロード
                                transferMain.checkFiles();

                                // 画像数確認と画像取得が終わるまで、ちょっと待機...
                                while (transferMain.isChecking())
                                {
                                    Thread.sleep(SLEEP_MS);
                                }
                                //long checkTime = Math.abs(System.currentTimeMillis() - checkStartTime);
                                //if (checkTime < SLEEP_WAIT_MS)
                                //{
                                //    // 画像数確認の時間が規定時間よりも短い場合は、しばらく待つ
                                //    Thread.sleep(SLEEP_WAIT_MS - checkTime);
                                //}

                                // 一定時間しばらく待つ (急ぎすぎると、GR2の電源が落ちる...
                                Thread.sleep(SLEEP_WAIT_MS);
                            }
                            //count++;
                            //Log.v(TAG, "TRANSFER LOOP : " + count);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    if (transferMain != null)
                    {
                        // 後処理...
                        transferMain.finish();
                    }
                    startTransferReceived = false;
                }
            });

            // 転送の開始
            transferThreadIsRunning = true;
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     *   転送終了
     *
     */
    private void finishTransfer()
    {
        try
        {
            // 転送モードを止める
            transferThreadIsRunning = false;

            // STARTボタンを有効化
            controlButton(true);

            // ぶるぶるする
            Vibrator vibrator = (activity != null) ? (Vibrator) activity.getSystemService(VIBRATOR_SERVICE) : null;
            if (vibrator != null)
            {
                vibrator.vibrate(150);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   画面上のボタンを制御する
     *
     */
    private void controlButton(boolean isStartButtonEnable)
    {
        try
        {
            Button start = activity.findViewById(R.id.transfer_start_button);
            Button stop = activity.findViewById(R.id.transfer_stop_button);
            if ((start != null)&&(stop != null))
            {
                start.setEnabled(isStartButtonEnable);
                stop.setEnabled(!isStartButtonEnable);
                CheckBox check = activity.findViewById(R.id.check_auto_download_raw);
                CheckBox original = activity.findViewById(R.id.check_auto_download_original);
                ProgressBar bar = activity.findViewById(R.id.auto_transfer_progress_bar);
                ImageButton reload = activity.findViewById(R.id.button_reload);
                ImageButton connect = activity.findViewById(R.id.button_wifi_connect);
                if (isStartButtonEnable)
                {
                    if (bar != null)
                    {
                        bar.setVisibility(View.GONE);
                    }
                    if (check != null)
                    {
                        check.setEnabled(true);
                    }
                    if (original != null)
                    {
                        original.setEnabled(true);
                    }
                    if (reload != null)
                    {
                        reload.setVisibility(View.VISIBLE);
                        reload.setEnabled(true);
                    }
                    if (connect != null)
                    {
                        connect.setVisibility(View.VISIBLE);
                        connect.setEnabled(true);
                    }
                }
                else
                {
                    if (bar != null)
                    {
                        bar.setVisibility(View.VISIBLE);
                    }
                    if (check != null)
                    {
                        check.setEnabled(false);
                    }
                    if (original != null)
                    {
                        original.setEnabled(false);
                    }
                    if (reload != null)
                    {
                        reload.setEnabled(false);
                        reload.setVisibility(View.INVISIBLE);
                    }
                    if (connect != null)
                    {
                        connect.setEnabled(false);
                        connect.setVisibility(View.INVISIBLE);
                    }
                }
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                if (preferences != null)
                {
                    TextView textView = activity.findViewById(R.id.auto_download_information_text);
                    String connectionMethod = preferences.getString(IPreferencePropertyAccessor.CONNECTION_METHOD, "RICOH");
                    if (!connectionMethod.contains("RICOH"))
                    {
                        // FUJI Xシリーズ/OPCの場合(逆に言うと RICOH 以外)は、この画面の操作系統をすべて無効化する
                        start.setEnabled(false);
                        stop.setEnabled(false);
                        if (bar != null)
                        {
                            bar.setVisibility(View.GONE);
                        }
                        if (check != null)
                        {
                            check.setEnabled(false);
                        }
                        if (original != null)
                        {
                            original.setEnabled(false);
                        }
                        if (reload != null)
                        {
                            reload.setVisibility(View.VISIBLE);
                            reload.setEnabled(true);
                        }
                        if (connect != null)
                        {
                            connect.setVisibility(View.VISIBLE);
                            connect.setEnabled(true);
                        }
                        if (textView != null)
                        {
                            textView.setText(R.string.does_not_support_this_feature);
                        }
                    }
                    else
                    {
                        if (textView != null)
                        {
                            textView.setText("");
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.transfer_start_button)
        {
            //
            Log.v(TAG, "TRANSFER START");
            startTransfer();
        }
        else if  (id == R.id.transfer_stop_button)
        {
            Log.v(TAG, "TRANSFER FINISH");
            finishTransfer();
        }
    }

    /**
     *
     *
     */
    @Override
    public void storedImage(@NonNull final String filename, final Bitmap picture)
    {
        if (activity != null)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    TextView textView = activity.findViewById(R.id.image_view_information);
                    if (textView != null)
                    {
                        textView.setText(filename);
                    }
                    try
                    {
                        if (picture != null)
                        {
                            ImageView imageView = activity.findViewById(R.id.image_view_area);
                            if (imageView != null)
                            {
                                imageView.setImageBitmap(picture);
                            }
                        }
                    }
                    catch (Throwable t)
                    {
                        t.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     *
     *
     */
    @Override
    public void showInformation(@NonNull final String message)
    {
        if (activity != null)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    TextView textView = activity.findViewById(R.id.auto_download_information_text);
                    if (textView != null)
                    {
                        textView.setText(message);
                    }
                }
            });
        }
    }

    // IContentDownloadNotify
    @Override
    public void downloadedImage(final String contentInfo, final Uri content)
    {
        if (activity != null)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView imageView = activity.findViewById(R.id.image_view_area);
                    TextView textView = activity.findViewById(R.id.image_view_information);
                    try
                    {
                        if ((imageView != null)&&(content != null))
                        {
                            imageView.setImageURI(content);
                        }
                        if ((textView != null)&&(contentInfo != null))
                        {
                            textView.setText(contentInfo);
                        }
                    }
                    catch (Throwable t)
                    {
                        t.printStackTrace();
                    }
                }
            });
        }
    }
}
