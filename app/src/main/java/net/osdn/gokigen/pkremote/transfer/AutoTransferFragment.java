package net.osdn.gokigen.pkremote.transfer;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.scene.IChangeScene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import static android.content.Context.VIBRATOR_SERVICE;

public class AutoTransferFragment extends Fragment implements View.OnClickListener
{
    private final String TAG = this.toString();

    private IInterfaceProvider interfaceProvider = null;
    private IChangeScene changeScene = null;
    private AppCompatActivity activity = null;
    private View myView = null;

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
        this.changeScene = sceneSelector;
        this.interfaceProvider = interfaceProvider;
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

        // 画面を抜ける時には、自動転送を停止させる
        finishTransfer();
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

    private void startTransfer()
    {
        try
        {
            // STARTボタンを無効化
            controlButton(false);

            // ぶるぶるする
            Vibrator vibrator = (activity != null) ? (Vibrator) activity.getSystemService(VIBRATOR_SERVICE) : null;
            if (vibrator != null)
            {
                vibrator.vibrate(50);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void finishTransfer()
    {
        try
        {
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
}
