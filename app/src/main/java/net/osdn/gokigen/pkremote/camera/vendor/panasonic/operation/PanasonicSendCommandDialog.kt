package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.IPanasonicInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera;

public class PanasonicSendCommandDialog  extends DialogFragment implements View.OnClickListener
{
    private final String TAG = toString();
    private IPanasonicInterfaceProvider interfaceProvider = null;
    private IPanasonicCamera camera = null;
    private Dialog myDialog = null;
    private EditText service = null;
    private EditText parameter = null;
    private EditText command = null;
    private TextView responseArea = null;
    private static final int TIMEOUT_MS = 2000;

    /**
     *
     *
     */
    public static PanasonicSendCommandDialog newInstance(@NonNull IPanasonicInterfaceProvider interfaceProvider)
    {
        PanasonicSendCommandDialog instance = new PanasonicSendCommandDialog();
        instance.prepare(interfaceProvider);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        //arguments.putString("method", method);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    /**
     *
     *
     */
    private void prepare(@NonNull IPanasonicInterfaceProvider interfaceProvider)
    {
        //
        this.interfaceProvider = interfaceProvider;
        this.camera = interfaceProvider.getPanasonicCamera();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "AlertDialog::onPause()");
        try
        {
            if (myDialog != null)
            {
                myDialog.cancel();
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
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Activity activity = getActivity();

        // コマンド送信ダイアログの生成
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.panasonic_request_layout, null, false);
        alertDialog.setView(alertView);

        alertDialog.setIcon(R.drawable.ic_linked_camera_black_24dp);
        alertDialog.setTitle(activity.getString(R.string.dialog_panasonic_command_title_command));
        service = alertView.findViewById(R.id.edit_service);
        parameter = alertView.findViewById(R.id.edit_parameter);
        command = alertView.findViewById(R.id.edit_command);
        responseArea = alertView.findViewById(R.id.panasonic_command_response_value);
        final Button sendButton = alertView.findViewById(R.id.send_message_button);
        final Button toRunningButton = alertView.findViewById(R.id.change_to_liveview);
        final Button toPlaybackButton = alertView.findViewById(R.id.change_to_playback);

        toRunningButton.setOnClickListener(this);
        toPlaybackButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        alertDialog.setCancelable(true);
        try
        {
            if (service != null)
            {
                service.setText(activity.getText(R.string.panasonic_service_string));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // ボタンを設定する（実行ボタン）
        alertDialog.setPositiveButton(activity.getString(R.string.dialog_positive_execute),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });

        // ボタンを設定する (キャンセルボタン）
        alertDialog.setNegativeButton(activity.getString(R.string.dialog_negative_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // 確認ダイアログを応答する
        myDialog = alertDialog.create();
        return (myDialog);
    }

    private void changeRunMode(boolean isStartLiveView)
    {
        // ライブビューの停止と開始
        Log.v(TAG, "changeRunMode() : " + isStartLiveView);
        ILiveViewControl liveViewControl = interfaceProvider.getPanasonicLiveViewControl();
        try
        {
            if (isStartLiveView)
            {
                liveViewControl.startLiveView(false);
            }
            else
            {
                liveViewControl.stopLiveView();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.change_to_liveview)
        {
            changeRunMode(true);
            return;
        }
        if (view.getId() == R.id.change_to_playback)
        {
            changeRunMode(false);
            return;
        }


        try
        {
            String serviceStr = "";
            String commandStr = "";
            String parameterStr = "";
            final Activity activity = getActivity();
            if (activity != null)
            {
                if (service != null)
                {
                    serviceStr = service.getText().toString();
                }
                if (command != null)
                {
                    commandStr = command.getText().toString();
                }
                final boolean isPost = (serviceStr.contains("post"));
                if (parameter != null)
                {
                    parameterStr = parameter.getText().toString();
                    if ((!isPost)&&(parameterStr.length() > 0))
                    {
                        commandStr = commandStr + "&" + parameterStr;
                    }
                }
                if (serviceStr.contains("pic"))
                {
                    serviceStr = camera.getPictureUrl() + commandStr;
                }
                else if (serviceStr.contains("obj"))
                {
                    serviceStr = camera.getObjUrl() + commandStr;
                }
                else
                {
                    serviceStr = camera.getCmdUrl() + serviceStr + "?" + commandStr;
                }
                final String url = serviceStr;
                final String param = parameterStr;

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run()
                    {
                        try
                        {
                            String reply;
                            if (isPost)
                            {
                                reply = SimpleHttpClient.httpPost(url, param, TIMEOUT_MS);
                            }
                            else
                            {
                                reply = SimpleHttpClient.httpGet(url, TIMEOUT_MS);
                            }
                            Log.v(TAG, "URL : " + url + " RESPONSE : " + reply);
                            final String response = reply;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (responseArea != null)
                                    {
                                        responseArea.setText(response);
                                    }
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
            else
            {
                Log.v(TAG, "getActivity() Fail...");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
