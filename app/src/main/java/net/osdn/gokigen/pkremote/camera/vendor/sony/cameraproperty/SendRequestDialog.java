package net.osdn.gokigen.pkremote.camera.vendor.sony.cameraproperty;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraApi;

/**
 *
 *
 */
public class SendRequestDialog  extends DialogFragment
{
    private final String TAG = toString();
    private ISonyCameraApi cameraApi;
    private String method = "";
    private int selectedPosition = 0;
    private SendRequestDialog.Callback callback = null;
    Dialog myDialog = null;

    /**
     *
     *
     */
    public static SendRequestDialog newInstance(@NonNull ISonyCameraApi cameraApi, @NonNull  String method, @Nullable SendRequestDialog.Callback callback)
    {
        SendRequestDialog instance = new SendRequestDialog();
        instance.prepare(cameraApi, method, callback);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        arguments.putString("method", method);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    /**
     *
     *
     */
    private void prepare(@NonNull ISonyCameraApi cameraApi,@NonNull  String method,  @Nullable SendRequestDialog.Callback callback)
    {
        this.cameraApi = cameraApi;
        this.method = method;
        this.callback = callback;
    }

    /**
     *
     *
     */
    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Activity activity = getActivity();

        // 確認ダイアログの生成
        //final AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.wear2_dialog_theme));
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.request_edit_layout, null, false);
        alertDialog.setView(alertView);

        alertDialog.setIcon(R.drawable.ic_linked_camera_black_24dp);
        alertDialog.setTitle("API : " + method);
        final Spinner spinner = alertView.findViewById(R.id.spinner_selection_service);
        final TextView methodName = alertView.findViewById(R.id.method_name);
        final EditText parameter = alertView.findViewById(R.id.edit_parameter);
        final EditText version = alertView.findViewById(R.id.edit_version);
        try {
            methodName.setText("");
            version.setText(activity.getString(R.string.dialog_version_hint));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
            adapter.addAll(cameraApi.getSonyApiServiceList());

            int defaultSelection;
            for (defaultSelection = (adapter.getCount() - 1); defaultSelection >= 0; defaultSelection--)
            {
                String item = adapter.getItem(defaultSelection);
                if ((item != null) && (item.equals("camera")))
                {
                    break;
                }
            }
            if ((defaultSelection < 0) || (defaultSelection >= adapter.getCount()))
            {
                defaultSelection = 0;
            }
            spinner.setAdapter(adapter);
            spinner.setSelection(defaultSelection);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    Log.v(TAG, "onItemSelected : " + position + " (" + id + ")");
                    selectedPosition = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {
                    Log.v(TAG, "onNothingSelected");
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        alertDialog.setCancelable(true);

        // ボタンを設定する（実行ボタン）
        alertDialog.setPositiveButton(activity.getString(R.string.dialog_positive_execute),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        try
                        {
                            Activity activity = getActivity();
                            if (activity != null)
                            {
                                if (callback != null)
                                {
                                    callback.sendRequest((String) spinner.getAdapter().getItem(selectedPosition), method, parameter.getText().toString(), version.getText().toString());
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            if (callback != null)
                            {
                                callback.cancelled();
                            }
                        }
                        dialog.dismiss();
                    }
                });

        // ボタンを設定する (キャンセルボタン）
        alertDialog.setNegativeButton(activity.getString(R.string.dialog_negative_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null)
                        {
                            callback.cancelled();
                        }
                        dialog.cancel();
                    }
                });

        // 確認ダイアログを応答する
        myDialog = alertDialog.create();
        return (myDialog);
    }


    @Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "AlertDialog::onPause()");
        if (myDialog != null)
        {
            myDialog.cancel();
        }
    }

    /**
     * コールバックインタフェース
     *
     */
    public interface Callback
    {
        void sendRequest(String service, String apiName, String parameter, String version); // OKを選択したとき
        void cancelled();                                                                  // キャンセルしたとき
    }
}
