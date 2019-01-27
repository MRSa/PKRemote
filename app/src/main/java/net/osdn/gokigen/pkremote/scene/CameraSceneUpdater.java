package net.osdn.gokigen.pkremote.scene;

import android.util.Log;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IStatusViewDrawer;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.logcat.LogCatFragment;
import net.osdn.gokigen.pkremote.preference.ricohgr2.RicohGr2PreferenceFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;

/**
 *
 *
 */
public class CameraSceneUpdater implements ICameraStatusReceiver, IChangeScene
{
    private final String TAG = toString();
    private final AppCompatActivity activity;
    private IInterfaceProvider interfaceProvider;
    private IStatusViewDrawer statusViewDrawer;

    private PreferenceFragmentCompat preferenceFragment = null;
    private LogCatFragment logCatFragment = null;

    public static CameraSceneUpdater newInstance(@NonNull AppCompatActivity activity)
    {
        return (new CameraSceneUpdater(activity));
    }

    /**
     *  コンストラクタ
     *
     */
    private CameraSceneUpdater(@NonNull AppCompatActivity activity)
    {
        this.activity = activity;
    }

    //  CameraSceneUpdater
    public void registerInterface(@NonNull IStatusViewDrawer statusViewDrawer, @NonNull IInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, "registerInterface()");
        this.statusViewDrawer = statusViewDrawer;
        this.interfaceProvider = interfaceProvider;
    }

    // ICameraStatusReceiver
    @Override
    public void onStatusNotify(String message)
    {
        Log.v(TAG, " CONNECTION MESSAGE : " + message);
        try
        {
            if (statusViewDrawer != null)
            {
                statusViewDrawer.updateStatusView(message);
                ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
                if (connection != null)
                {
                    statusViewDrawer.updateConnectionStatus(connection.getConnectionStatus());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ICameraStatusReceiver
    @Override
    public void onCameraConnected()
    {
        Log.v(TAG, "onCameraConnected()");

        try
        {
            ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
            if (connection != null)
            {
                connection.forceUpdateConnectionStatus(ICameraConnection.CameraConnectionStatus.CONNECTED);
            }
            if (statusViewDrawer != null)
            {
                statusViewDrawer.updateConnectionStatus(ICameraConnection.CameraConnectionStatus.CONNECTED);

                // ライブビューの開始...
                statusViewDrawer.startLiveView();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ICameraStatusReceiver
    @Override
    public void onCameraDisconnected()
    {
        Log.v(TAG, "onCameraDisconnected()");
        if (statusViewDrawer != null)
        {
            statusViewDrawer.updateStatusView(activity.getString(R.string.camera_disconnected));
            statusViewDrawer.updateConnectionStatus(ICameraConnection.CameraConnectionStatus.DISCONNECTED);
        }
    }

    // ICameraStatusReceiver
    @Override
    public void onCameraOccursException(String message, Exception e)
    {
        Log.v(TAG, "onCameraOccursException() " + message);
        try
        {
            e.printStackTrace();
            ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
            if (connection != null)
            {
                connection.alertConnectingFailed(message + " " + e.getLocalizedMessage());
            }
            if (statusViewDrawer != null)
            {
                statusViewDrawer.updateStatusView(message);
                if (connection != null)
                {
                    statusViewDrawer.updateConnectionStatus(connection.getConnectionStatus());
                }
            }
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }
    }

    //  IChangeScene
    @Override
    public void changeSceneToCameraPropertyList()
    {
/*
        try
        {
            ICameraConnection.CameraConnectionMethod method = interfaceProvider.getCammeraConnectionMethod();
            ICameraConnection connection = getCameraConnection(method);
            if (method == ICameraConnection.CameraConnectionMethod.RICOH_GR2)
            {
                // OPCカメラでない場合には、「OPCカメラのみ有効です」表示をして画面遷移させない
                Toast.makeText(getApplicationContext(), getText(R.string.only_opc_feature), Toast.LENGTH_SHORT).show();
            }
            else if (method == ICameraConnection.CameraConnectionMethod.SONY)
            {
                // OPCカメラでない場合には、「OPCカメラのみ有効です」表示をして画面遷移させない
                Toast.makeText(getApplicationContext(), getText(R.string.only_opc_feature), Toast.LENGTH_SHORT).show();
            }
            else
            {
                // OPC カメラの場合...
                if (connection != null)
                {
                    ICameraConnection.CameraConnectionStatus status = connection.getConnectionStatus();
                    if (status == ICameraConnection.CameraConnectionStatus.CONNECTED)
                    {
                        if (propertyListFragment == null)
                        {
                            propertyListFragment = OlyCameraPropertyListFragment.newInstance(this, interfaceProvider.getOlympusInterface().getCameraPropertyProvider());
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment1, propertyListFragment);
                        // backstackに追加
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
*/
    }

    //  IChangeScene
    @Override
    public void changeSceneToConfiguration()
    {
        try
        {
            if (preferenceFragment == null)
            {
                try
                {
                    preferenceFragment = RicohGr2PreferenceFragment.newInstance(activity, this);
/*
                    ICameraConnection.CameraConnectionMethod connectionMethod = interfaceProvider.getCammeraConnectionMethod();
                    if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH_GR2) {
                        preferenceFragment = RicohGr2PreferenceFragment.newInstance(activity, this);
                    //} else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY) {
                    //    preferenceFragment = SonyPreferenceFragment.newInstance(this, this);
                    } else //  if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
                    {
                        preferenceFragment = PreferenceFragment.newInstance(activity, interfaceProvider, this);
                    }
*/
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    //preferenceFragment = SonyPreferenceFragment.newInstance(this, this);
                }
            }

            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment1, preferenceFragment);
            // backstackに追加
            transaction.addToBackStack(null);
            transaction.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //  IChangeScene
    @Override
    public void changeCameraConnection()
    {
        if (interfaceProvider == null)
        {
            Log.v(TAG, "changeCameraConnection() : interfaceProvider is NULL");
            return;
        }
        try
        {
            interfaceProvider.resetCameraConnectionMethod();
            ICameraConnection connection = interfaceProvider.getCameraConnection();
            if (connection != null)
            {
                ICameraConnection.CameraConnectionStatus status = connection.getConnectionStatus();
                if (status == ICameraConnection.CameraConnectionStatus.CONNECTED)
                {
                    // 接続中のときには切断する
                    connection.disconnect(false);
                    return;
                }
                // 接続中でない時は、接続中にする
                connection.startWatchWifiStatus(activity);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //  IChangeScene
    @Override
    public void changeSceneToDebugInformation()
    {
        if (logCatFragment == null)
        {
            logCatFragment = LogCatFragment.newInstance();
        }
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, logCatFragment);
        // backstackに追加
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //  IChangeScene
    @Override
    public void changeSceneToApiList()
    {
/*
        if (sonyApiListFragmentSony == null)
        {
            sonyApiListFragmentSony = SonyCameraApiListFragment.newInstance(interfaceProvider);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, sonyApiListFragmentSony);
        // backstackに追加
        transaction.addToBackStack(null);
        transaction.commit();
*/
    }

    /**
     *   画像一覧画面を開く
     *
     */
/*
    //  IChangeScene
    @Override
    public void changeScenceToImageList()
    {
        Log.v(TAG, "changeScenceToImageList()");
        try
        {
            ImageGridViewFragment fragment = ImageGridViewFragment.newInstance(interfaceProvider.getPlaybackControl(), interfaceProvider.getCameraRunMode());
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment1, fragment);
            // backstackに追加
            transaction.addToBackStack(null);
            transaction.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
*/

    //  IChangeScene
    @Override
    public void exitApplication()
    {
        Log.v(TAG, "exitApplication()");
        try
        {
            ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
            if (connection != null)
            {
                connection.disconnect(true);
            }
            activity.finish();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private ICameraConnection getCameraConnection(ICameraConnection.CameraConnectionMethod method)
    {
        Log.v(TAG, "method : " + method);
        return (interfaceProvider.getCameraConnection());
    }
}
