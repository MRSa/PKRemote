package net.osdn.gokigen.pkremote.scene;

import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.calendar.CalendarFragment;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentsRecognizer;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.sony.cameraproperty.SonyCameraApiListFragment;
import net.osdn.gokigen.pkremote.logcat.LogCatFragment;
import net.osdn.gokigen.pkremote.playback.ImageGridViewFragment;
import net.osdn.gokigen.pkremote.preference.canon.CanonPreferenceFragment;
import net.osdn.gokigen.pkremote.preference.fujix.FujiXPreferenceFragment;
import net.osdn.gokigen.pkremote.preference.nikon.NikonPreferenceFragment;
import net.osdn.gokigen.pkremote.preference.olympus.OpcPreferenceFragment;
import net.osdn.gokigen.pkremote.preference.olympuspen.OlympusPenPreferenceFragment;
import net.osdn.gokigen.pkremote.preference.panasonic.PanasonicPreferenceFragment;
import net.osdn.gokigen.pkremote.preference.ricohgr2.RicohGr2PreferenceFragment;
import net.osdn.gokigen.pkremote.preference.sony.SonyPreferenceFragment;
import net.osdn.gokigen.pkremote.transfer.AutoTransferFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;

/**
 *
 *
 */
public class CameraSceneUpdater implements ICameraStatusReceiver, IChangeScene, ICameraContentsRecognizer.ICameraContentsListCallback
{
    private final String TAG = toString();
    private final AppCompatActivity activity;
    private final BottomNavigationView bottomNavigationView;
    private IInterfaceProvider interfaceProvider;
    //private IStatusViewDrawer statusViewDrawer;
    private ICameraStatusReceiver anotherStatusReceiver = null;
    private PreferenceFragmentCompat preferenceFragment = null;
    private LogCatFragment logCatFragment = null;
    private CalendarFragment calendarFragment = null;
    private ImageGridViewFragment gridViewFragment = null;
    private AutoTransferFragment autoTransferFragment = null;
    private SonyCameraApiListFragment sonyApiListFragmentSony = null;

    public static CameraSceneUpdater newInstance(@NonNull AppCompatActivity activity)
    {
        return (new CameraSceneUpdater(activity));
    }

    /**
     * コンストラクタ
     */
    private CameraSceneUpdater(@NonNull AppCompatActivity activity)
    {
        this.activity = activity;
        this.bottomNavigationView = activity.findViewById(R.id.navigation);
    }

    /**
     * 一番最初のフラグメントを表示する
     */
    public void changeFirstFragment(@NonNull IInterfaceProvider interfaceProvider)
    {
        this.interfaceProvider = interfaceProvider;
        try
        {
            bottomNavigationView.setSelectedItemId(R.id.navigation_calendar);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // 初期画面(カレンダー画面)へ遷移
        changeSceneToCalendar();
    }

/*
    //  CameraSceneUpdater
    public void registerInterface(@NonNull IStatusViewDrawer statusViewDrawer, @NonNull IInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, "registerInterface()");
        this.statusViewDrawer = statusViewDrawer;
        this.interfaceProvider = interfaceProvider;
    }
*/

    // ICameraStatusReceiver
    @Override
    public void onStatusNotify(String message)
    {
        Log.v(TAG, " CONNECTION MESSAGE : " + message);
        try {
/*
            if (statusViewDrawer != null)
            {
                statusViewDrawer.updateStatusView(message);
                ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
                if (connection != null) {
                    statusViewDrawer.updateConnectionStatus(connection.getConnectionStatus());
                }
            }
*/
            if (anotherStatusReceiver != null)
            {
                anotherStatusReceiver.onStatusNotify(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ICameraStatusReceiver
    @Override
    public void onCameraConnected() {
        Log.v(TAG, "onCameraConnected()");
        updateConnectionStatus(activity.getString(R.string.connect_connected), ICameraConnection.CameraConnectionStatus.CONNECTED);
        try {
            ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
            if (connection != null) {
                connection.forceUpdateConnectionStatus(ICameraConnection.CameraConnectionStatus.CONNECTED);
            }
/*
            if (statusViewDrawer != null)
            {
                statusViewDrawer.updateConnectionStatus(ICameraConnection.CameraConnectionStatus.CONNECTED);

                // ライブビューの開始... 今回は手動化。
                //statusViewDrawer.startLiveView();
            }
*/
            if (anotherStatusReceiver != null) {
                anotherStatusReceiver.onCameraConnected();
            }
            ICameraContentsRecognizer recognizer = interfaceProvider.getCameraContentsRecognizer();
            if (recognizer != null) {
                // カメラ内のコンテンツ一覧を作成するように指示する
                recognizer.getRemoteCameraContentsList(true, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ICameraStatusReceiver
    @Override
    public void onCameraDisconnected() {
        Log.v(TAG, "onCameraDisconnected()");
        String message = activity.getString(R.string.camera_disconnected);
        updateConnectionStatus(message, ICameraConnection.CameraConnectionStatus.DISCONNECTED);
        try {
/*
            if (statusViewDrawer != null) {
                statusViewDrawer.updateStatusView(activity.getString(R.string.camera_disconnected));
                statusViewDrawer.updateConnectionStatus(ICameraConnection.CameraConnectionStatus.DISCONNECTED);
            }
*/
            if (anotherStatusReceiver != null) {
                anotherStatusReceiver.onCameraDisconnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ICameraStatusReceiver
    @Override
    public void onCameraOccursException(String message, Exception e) {
        Log.v(TAG, "onCameraOccursException() " + message);
        try {
            ICameraConnection.CameraConnectionStatus connectionStatus;  // = ICameraConnection.CameraConnectionStatus.UNKNOWN;

            e.printStackTrace();
            ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
            if (connection != null) {
                connectionStatus = connection.getConnectionStatus();
                connection.alertConnectingFailed(message + " " + e.getLocalizedMessage());
                updateConnectionStatus(message, connectionStatus);
            }
/*
            if (statusViewDrawer != null) {
                statusViewDrawer.updateStatusView(message);
                if (connection != null) {
                    statusViewDrawer.updateConnectionStatus(connectionStatus);
                }
            }
*/
            if (anotherStatusReceiver != null) {
                anotherStatusReceiver.onCameraOccursException(message, e);
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    /**
     * カメラとの接続状態を表示更新する
     */
    private void updateConnectionStatus(final String message, final ICameraConnection.CameraConnectionStatus status) {
        try {
            final int resId;
            switch (status) {
                case CONNECTED:
                    resId = R.drawable.ic_cloud_done_black_24dp;
                    break;
                case CONNECTING:
                    resId = R.drawable.ic_cloud_queue_black_24dp;
                    break;
                case DISCONNECTED:
                    resId = R.drawable.ic_cloud_off_black_24dp;
                    break;
                case UNKNOWN:
                default:
                    resId = R.drawable.ic_cloud_queue_grey_24dp;
                    break;
            }
            final TextView messageArea = activity.findViewById(R.id.message);
            final ImageButton buttonArea = activity.findViewById(R.id.button_wifi_connect);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (buttonArea != null) {
                            buttonArea.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), resId, null));
                            buttonArea.invalidate();
                        }
                        if ((messageArea != null) && (message != null)) {
                            messageArea.setText(message);
                            messageArea.invalidate();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
            if (method == ICameraConnection.CameraConnectionMethod.RICOH)
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
                    //preferenceFragment = RicohGr2PreferenceFragment.newInstance(activity, this);
                    ICameraConnection.CameraConnectionMethod connectionMethod = interfaceProvider.getCammeraConnectionMethod();
                    if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
                    {
                        preferenceFragment = RicohGr2PreferenceFragment.newInstance(activity, this);
                    }
                    else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
                    {
                        preferenceFragment = FujiXPreferenceFragment.newInstance(activity, this);
                        //} else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY) {
                        //    preferenceFragment = SonyPreferenceFragment.newInstance(this, this);
                    }
                    else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
                    {
                        preferenceFragment = PanasonicPreferenceFragment.newInstance(activity, this);
                    }
                    else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
                    {
                        preferenceFragment = SonyPreferenceFragment.newInstance(activity, this);
                    }
                    else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
                    {
                        preferenceFragment = CanonPreferenceFragment.newInstance(activity, this);
                    }
                    else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
                    {
                        preferenceFragment = NikonPreferenceFragment.newInstance(activity, this);
                    }
                    else if (connectionMethod == ICameraConnection.CameraConnectionMethod.OLYMPUS)
                    {
                        preferenceFragment = OlympusPenPreferenceFragment.newInstance(activity, this);
                    }
                    else //  if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
                    {
                        preferenceFragment = OpcPreferenceFragment.newInstance(activity, interfaceProvider, this);
                    }
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
    public void reloadRemoteImageContents()
    {
        try
        {
            ICameraContentsRecognizer recognizer = interfaceProvider.getCameraContentsRecognizer();
            if (recognizer != null) {
                // カメラ内のコンテンツ一覧を作成するように指示する
                recognizer.getRemoteCameraContentsList(true, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  IChangeScene
    @Override
    public void changeSceneToDebugInformation()
    {
        if (logCatFragment == null) {
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
        if (sonyApiListFragmentSony == null)
        {
            sonyApiListFragmentSony = SonyCameraApiListFragment.newInstance(interfaceProvider);
        }
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, sonyApiListFragmentSony);
        // backstackに追加
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //  IChangeScene
    @Override
    public void changeSceneToAutoTransfer()
    {
        if (autoTransferFragment == null)
        {
            autoTransferFragment = AutoTransferFragment.newInstance(activity, interfaceProvider);
        }
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, autoTransferFragment);
        // backstackに追加
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //  IChangeScene
    @Override
    public void changeSceneToCalendar()
    {
        if (calendarFragment == null)
        {
            calendarFragment = CalendarFragment.newInstance(activity, this, interfaceProvider);
        }
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, calendarFragment);
        // backstackに追加
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //  IChangeScene
    @Override
    public void changeScenceDateSelected(String filterLabel)
    {
        Log.v(TAG, "changeScenceDateSelected()");

        bottomNavigationView.setSelectedItemId(R.id.navigation_photo_library);
        changeScenceToImageList(filterLabel);
    }

    /**
     * 画像一覧画面を開く
     */
    //  IChangeScene
    @Override
    public void changeScenceToImageList()
    {
        changeScenceToImageList(null);
    }

    /**
     *
     *
     */
    private void changeScenceToImageList(String filterLabel)
    {
        Log.v(TAG, "changeScenceToImageList() : " + filterLabel);
        try
        {
            if (gridViewFragment == null)
            {
                gridViewFragment = ImageGridViewFragment.newInstance(interfaceProvider);
            }
            gridViewFragment.setFilterLabel(filterLabel);
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment1, gridViewFragment);
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

    @Override
    public void updateBottomNavigationMenu()
    {
        try
        {
            // ナビゲーション部分の選択状態をしたい...
            int changeId = 0;
            if (calendarFragment.isFragmentActive())
            {
                changeId = R.id.navigation_calendar;
            }
            else if (gridViewFragment.isFragmentActive())
            {
                changeId = R.id.navigation_photo_library;
            }
            if ((bottomNavigationView != null)&&(changeId != 0))
            {
                bottomNavigationView.setSelectedItemId(changeId);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setAnotherStatusReceiver(ICameraStatusReceiver statusReceiver)
    {
        this.anotherStatusReceiver = statusReceiver;
    }

    private ICameraConnection getCameraConnection(ICameraConnection.CameraConnectionMethod method)
    {
        Log.v(TAG, "method : " + method);
        return (interfaceProvider.getCameraConnection());
    }

    @Override
    public void contentsListCreated(int nofContents)
    {
        Log.v(TAG, "contentsListCreated() : " + nofContents);

        // カレンダー画面のリフレッシュを行いたい (かなり無理やり...)
        if ((calendarFragment != null)&&(calendarFragment.isFragmentActive()))
        {
            calendarFragment.contentsListCreated(nofContents);
        }
    }
}
