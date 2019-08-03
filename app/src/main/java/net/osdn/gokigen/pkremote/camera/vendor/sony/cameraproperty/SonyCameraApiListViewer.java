package net.osdn.gokigen.pkremote.camera.vendor.sony.cameraproperty;

import android.util.Log;

import androidx.preference.Preference;

import net.osdn.gokigen.pkremote.scene.IChangeScene;

/**
 *
 *
 */
public class SonyCameraApiListViewer implements Preference.OnPreferenceClickListener
{
    private final String TAG = toString();
    private final IChangeScene changeScene;

    /**
     *
     *
     */    public SonyCameraApiListViewer(IChangeScene changeScene)
    {
        this.changeScene = changeScene;
    }

    /**
     *
     *
     */    public void prepare()
    {
        Log.v(TAG, "prepare() ");
    }

    /**
     *
     *
     */
    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        if (!preference.hasKey())
        {
            return (false);
        }

        String preferenceKey = preference.getKey();
        if ((preferenceKey.contains("sony_api_list"))&&(changeScene != null))
        {
            try
            {
                // API Listを表示する
                changeScene.changeSceneToApiList();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return (true);
        }
        return (false);
    }
}
