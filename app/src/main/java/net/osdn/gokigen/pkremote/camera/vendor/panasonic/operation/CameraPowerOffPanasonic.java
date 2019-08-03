package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation;

import android.content.Context;

import androidx.preference.Preference;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;
import net.osdn.gokigen.pkremote.scene.ConfirmationDialog;
import net.osdn.gokigen.pkremote.scene.IChangeScene;

/**
 *  Preferenceがクリックされた時に処理するクラス
 *
 */
public class CameraPowerOffPanasonic implements Preference.OnPreferenceClickListener, ConfirmationDialog.Callback
{

    private final Context context;
    private final IChangeScene changeScene;
    private String preferenceKey = null;

    /**
     *   コンストラクタ
     *
     */
    public CameraPowerOffPanasonic(Context context, IChangeScene changeScene)
    {
        this.context = context;
        this.changeScene = changeScene;
    }

    /**
     *   クラスの準備
     *
     */
    public void prepare()
    {
        // 何もしない
    }

    /**
     *
     *
     * @param preference クリックしたpreference
     * @return false : ハンドルしない / true : ハンドルした
     */
    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        if (!preference.hasKey())
        {
            return (false);
        }

        preferenceKey = preference.getKey();
        if (preferenceKey.contains(IPreferencePropertyAccessor.EXIT_APPLICATION))
        {

            // 確認ダイアログの生成と表示
            ConfirmationDialog dialog = ConfirmationDialog.newInstance(context);
            dialog.show(R.string.dialog_title_confirmation, R.string.dialog_message_exit_application, this);
            return (true);
        }
        return (false);
    }

    /**
     *
     *
     */
    @Override
    public void confirm()
    {
        if (preferenceKey.contains(IPreferencePropertyAccessor.EXIT_APPLICATION))
        {
            // カメラの電源をOFFにしたうえで、アプリケーションを終了する。
            changeScene.exitApplication();
        }
    }
}

