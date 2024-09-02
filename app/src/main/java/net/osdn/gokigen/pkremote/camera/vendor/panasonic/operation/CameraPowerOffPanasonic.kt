package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation

import android.content.Context
import androidx.preference.Preference
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor
import net.osdn.gokigen.pkremote.scene.ConfirmationDialog
import net.osdn.gokigen.pkremote.scene.IChangeScene

/**
 * Preferenceがクリックされた時に処理するクラス
 *
 */
class CameraPowerOffPanasonic(private val context: Context, private val changeScene: IChangeScene) : Preference.OnPreferenceClickListener, ConfirmationDialog.Callback
{
    private var preferenceKey: String? = null

    fun prepare()
    {
        // 何もしない
    }

    override fun onPreferenceClick(preference: Preference): Boolean
    {
        try
        {
            if (!preference.hasKey())
            {
                return (false)
            }
            preferenceKey = preference.key
            if (preferenceKey?.contains(IPreferencePropertyAccessor.EXIT_APPLICATION) == true)
            {
                // 確認ダイアログの生成と表示
                val dialog = ConfirmationDialog.newInstance(context)
                dialog.show(R.string.dialog_title_confirmation, R.string.dialog_message_exit_application, this)
                return (true)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    override fun confirm()
    {
        if (preferenceKey?.contains(IPreferencePropertyAccessor.EXIT_APPLICATION) == true)
        {
            // カメラの電源をOFFにしたうえで、アプリケーションを終了する。
            changeScene.exitApplication()
        }
    }
}