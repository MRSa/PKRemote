package net.osdn.gokigen.pkremote.calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import net.osdn.gokigen.pkremote.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 *    年と月を開くダイアログ
 *
 */
public class TargetMonthSetDialog extends DialogFragment
{
    private final String TAG = toString();
    private static final int YEAR_LIMIT_MIN = 2010;
    private static final int YEAR_LIMIT_MAX = 2050;
    private static final int MONTH_LIMIT_MIN = 1;
    private static final int MONTH_LIMIT_MAX = 12;

    private String title = "";
    private int yearNum = 0;
    private int monthNum = 0;
    private Callback callback = null;
    private Dialog myDialog = null;

    /**
     *
     *
     */
    public static TargetMonthSetDialog newInstance(String title, int year, int month, Callback callback)
    {
        TargetMonthSetDialog instance = new TargetMonthSetDialog();
        instance.prepare(title, year, month, callback);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        arguments.putString("title", title);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    /**
     *
     *
     */
    private void prepare(String title, int yearNum, int monthNum, Callback callback)
    {
        this.title = title;
        this.yearNum = yearNum;
        this.monthNum = monthNum;
        this.callback = callback;
    }

    /**
     *
     *
     */
    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Log.v(TAG, "show " +   yearNum + " / " + monthNum + " ");

        Activity activity = getActivity();

        // 確認ダイアログの生成
        //final AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.wear2_dialog_theme));
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.target_month_picker, null, false);
        alertDialog.setView(alertView);

        final TextView titleText = alertView.findViewById(R.id.information_picker);
        final NumberPicker month = alertView.findViewById(R.id.number_picker_month);
        final NumberPicker year = alertView.findViewById(R.id.number_picker_year);

        try
        {
            if (title != null)
            {
                titleText.setText(title);
            }
            month.setMinValue(MONTH_LIMIT_MIN);
            month.setMaxValue(MONTH_LIMIT_MAX);
            year.setMinValue(YEAR_LIMIT_MIN);
            year.setMaxValue(YEAR_LIMIT_MAX);

            month.setValue(monthNum);
            year.setValue(yearNum);
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
                            Log.v(TAG, "ENTRY [" + year.getValue() + " / " + month.getValue() + "] ");
                            callback.dataSetYearMonth(year.getValue(), month.getValue());
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            callback.dataSetCancelled();
                        }
                        dialog.dismiss();
                    }
                });

        // ボタンを設定する (キャンセルボタン）
        alertDialog.setNegativeButton(activity.getString(R.string.dialog_negative_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        try
                        {
                            callback.dataSetCancelled();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        dialog.cancel();
                    }
                });

        myDialog = alertDialog.create();
        return (myDialog);
    }

    /**
     *
     *
     */
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
     *  コールバックインタフェースの定義
     *
     */
    public interface Callback
    {
        void dataSetYearMonth(int year, int month); // OKを選択したとき
        void dataSetCancelled();  // キャンセルしたとき
    }
}
