package net.osdn.gokigen.pkremote.playback;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import net.osdn.gokigen.pkremote.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 *   複数ファイルの一括ダウンロードを実施することを確認するダイアログ
 *
 */
public class MultiFileDownloadConfirmationDialog extends DialogFragment
{
    private final String TAG = this.toString();

    private Callback callback;
    private int nofPictures;

    public static MultiFileDownloadConfirmationDialog newInstance(@NonNull Callback callback, int nofPictures)
    {
        MultiFileDownloadConfirmationDialog instance = new MultiFileDownloadConfirmationDialog();
        instance.prepare(callback, nofPictures);

        return (instance);
    }

    private void prepare(Callback callback, int nofPictures)
    {
        this.callback = callback;
        this.nofPictures = nofPictures;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_confirmation_batch_download, null, false);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.dialog_title_batch_download));
        TextView textView = view.findViewById(R.id.label_batch_download_info);
        if (textView != null)
        {
            String label = getString(R.string.dialog_label_batch_download) + nofPictures;
            textView.setText(label);
        }
        final RadioGroup radio = view.findViewById(R.id.radio_group_select_category);
        final CheckBox checkWithRaw = view.findViewById(R.id.radio_download__raw);

        // ボタンを設定する（実行ボタン）
        builder.setPositiveButton(activity.getString(R.string.dialog_positive_download),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int buttonId = 0;
                        boolean withRaw = false;
                        try {
                            if (radio != null)
                            {
                                buttonId = radio.getCheckedRadioButtonId();
                            }
                            if (checkWithRaw != null)
                            {
                                withRaw = checkWithRaw.isChecked();
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                        //Log.v(TAG, "confirmSelection() " + buttonId + " [" + withRaw + "]");
                        callback.confirmSelection(buttonId, withRaw);
                     }
                });

        // ボタンを設定する (キャンセルボタン）
        builder.setNegativeButton(activity.getString(R.string.dialog_negative_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.setView(view);
        return (builder.create());
    }

    // コールバックインタフェース
    public interface Callback
    {
        void confirmSelection(int selectedButtonId, boolean withRaw);
    }

}
