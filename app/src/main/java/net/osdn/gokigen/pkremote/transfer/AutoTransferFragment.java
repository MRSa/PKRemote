package net.osdn.gokigen.pkremote.transfer;

import android.content.Context;
import android.graphics.Bitmap;
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
import net.osdn.gokigen.pkremote.scene.IChangeScene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 *   自動転送クラス
 *
 */
public class AutoTransferFragment extends Fragment implements View.OnClickListener, ITransferMessage
{
    private final String TAG = this.toString();

    private static final int SLEEP_MS = 3000;   // 待機時間

    private AppCompatActivity activity = null;
    private FileAutoTransferMain transferMain = null;
    private View myView = null;
    private boolean transferThreadIsRunning = false;

    public static AutoTransferFragment newInstance(@NonNull AppCompatActivity context, IChangeScene sceneSelector, @NonNull IInterfaceProvider provider)
    {
        AutoTransferFragment instance = new AutoTransferFragment();
        instance.prepare(context, sceneSelector, provider);

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
    private void prepare(@NonNull AppCompatActivity activity, IChangeScene sceneSelector, IInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, "prepare()");
        this.activity = activity;
        transferMain = new FileAutoTransferMain(activity, sceneSelector, interfaceProvider, this);
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
            // STARTボタンを無効化してぶるぶるする...
            controlButton(false);
            Vibrator vibrator = (activity != null) ? (Vibrator) activity.getSystemService(VIBRATOR_SERVICE) : null;
            if (vibrator != null)
            {
                vibrator.vibrate(50);
            }

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
                    int count = 0;
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
                                // チェックして追加ファイルがあったらダウンロード
                                transferMain.downloadFiles();
                            }
                            count++;
                            Log.v(TAG, "TRANSFER LOOP : " + count);

                            // ちょっと待機...
                            Thread.sleep(SLEEP_MS);
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
}
