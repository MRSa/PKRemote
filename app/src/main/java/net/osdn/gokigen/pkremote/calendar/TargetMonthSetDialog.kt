package net.osdn.gokigen.pkremote.calendar

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import net.osdn.gokigen.pkremote.R

class TargetMonthSetDialog : DialogFragment()
{
    private var dateCallback: DateCallback? = null

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        // 呼び出し元の Fragment または Activity から Callback を取得する
        val parentFragment = getParentFragment()
        if (parentFragment is DateCallback)
        {
            dateCallback = parentFragment as DateCallback
        }
        else if (context is DateCallback)
        {
            dateCallback = context as DateCallback
        }
        else
        {
            Log.w(TAG, "Do not implement callback in the caller(Activity/Fragment)")
        }
    }

    override fun onDetach()
    {
        super.onDetach()
        dateCallback = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        // Bundle から引数を取得（デフォルト値を設定）
        val args = arguments
        val title = if (args != null) {
            args.getString(KEY_TITLE, "")
        } else ""
        val yearNum = args?.getInt(KEY_YEAR, 2026) ?: 2026
        val monthNum = args?.getInt(KEY_MONTH, 1) ?: 1

        Log.v(TAG, "show $yearNum / $monthNum")

        val activity: Activity = requireActivity()
        val alertDialog = AlertDialog.Builder(activity)

        val inflater = activity.layoutInflater
        val alertView = inflater.inflate(R.layout.target_month_picker, null, false)
        alertDialog.setView(alertView)

        val titleText = alertView.findViewById<TextView?>(R.id.information_picker)
        val month = alertView.findViewById<NumberPicker?>(R.id.number_picker_month)
        val year = alertView.findViewById<NumberPicker?>(R.id.number_picker_year)

        if (titleText != null && title != null) {
            titleText.text = title
        }

        if (month != null) {
            month.setMinValue(MONTH_LIMIT_MIN)
            month.setMaxValue(MONTH_LIMIT_MAX)
            month.value = monthNum
        }

        if (year != null) {
            year.setMinValue(YEAR_LIMIT_MIN)
            year.setMaxValue(YEAR_LIMIT_MAX)
            year.value = yearNum
        }

        // 実行ボタンの設定
        alertDialog.setPositiveButton(
            R.string.dialog_positive_execute
        ) { _, _ ->
            if (dateCallback != null && year != null && month != null) {
                Log.v(TAG, "ENTRY [" + year.value + " / " + month.value + "]")
                dateCallback?.dataSetYearMonth(year.value, month.value)
            }
        }

        // キャンセルボタンの設定
        alertDialog.setNegativeButton(R.string.dialog_negative_cancel) { _, _ -> dateCallback?.dataSetCancelled() }

        return alertDialog.create()
    }

    /**
     * コールバックインタフェースの定義
     */
    interface DateCallback
    {
        // OKを選択したとき
        fun dataSetYearMonth(year: Int, month: Int)
        // キャンセルしたとき
        fun dataSetCancelled()
    }

    companion object {
        private val TAG: String = TargetMonthSetDialog::class.java.getSimpleName()

        private const val KEY_TITLE = "key_title"
        private const val KEY_YEAR = "key_year"
        private const val KEY_MONTH = "key_month"

        private const val YEAR_LIMIT_MIN = 2010
        private const val YEAR_LIMIT_MAX = 2050
        private const val MONTH_LIMIT_MIN = 1
        private const val MONTH_LIMIT_MAX = 12

        // インスタンス生成用ファクトリメソッド
        @JvmStatic
        fun newInstance(title: String?, year: Int, month: Int): TargetMonthSetDialog {
            val instance = TargetMonthSetDialog()

            // パラメータはすべて Bundle にまとめて保持させる
            val arguments = Bundle()
            arguments.putString(KEY_TITLE, title)
            arguments.putInt(KEY_YEAR, year)
            arguments.putInt(KEY_MONTH, month)
            instance.setArguments(arguments)

            return instance
        }
    }
}
