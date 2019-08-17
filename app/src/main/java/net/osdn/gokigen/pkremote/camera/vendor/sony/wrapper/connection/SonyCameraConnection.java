package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.connection;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraHolder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 *
 *
 */
public class SonyCameraConnection implements ICameraConnection
{
    private final String TAG = toString();
    private final Activity context;
    private final ICameraStatusReceiver statusReceiver;
    private final BroadcastReceiver connectionReceiver;
    private final ISonyCameraHolder cameraHolder;
    //private final ConnectivityManager connectivityManager;
    private final Executor cameraExecutor = Executors.newFixedThreadPool(1);
    private final ICameraChangeListener listener;
    //private final Handler networkConnectionTimeoutHandler;
    //private static final int MESSAGE_CONNECTIVITY_TIMEOUT = 1;
    private CameraConnectionStatus connectionStatus = CameraConnectionStatus.UNKNOWN;

    public SonyCameraConnection(final Activity context, final ICameraStatusReceiver statusReceiver, @NonNull ISonyCameraHolder cameraHolder, final @NonNull ICameraChangeListener listener)
    {
        Log.v(TAG, "SonyCameraConnection()");
        this.context = context;
        this.statusReceiver = statusReceiver;
        this.cameraHolder = cameraHolder;
        this.listener = listener;
/*
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkConnectionTimeoutHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case MESSAGE_CONNECTIVITY_TIMEOUT:
                        Log.d(TAG, "Network connection timeout");
                        alertConnectingFailed(context.getString(R.string.network_connection_timeout));
                        connectionStatus = CameraConnectionStatus.DISCONNECTED;
                        break;
                }
            }
        };
*/
        connectionReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                onReceiveBroadcastOfConnection(context, intent);
            }
        };
    }

    private void onReceiveBroadcastOfConnection(Context context, Intent intent)
    {
        statusReceiver.onStatusNotify(context.getString(R.string.connect_check_wifi));
        Log.v(TAG,context.getString(R.string.connect_check_wifi));

        String action = intent.getAction();
        if (action == null)
        {
            //
            Log.v(TAG, "intent.getAction() : null");
            return;
        }

        try
        {
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
            {
                Log.v(TAG, "onReceiveBroadcastOfConnection() : CONNECTIVITY_ACTION");

                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo info = wifiManager.getConnectionInfo();
                    if (wifiManager.isWifiEnabled() && info != null)
                    {
                        if (info.getNetworkId() != -1)
                        {
                            Log.v(TAG, "Network ID is -1, there is no currently connected network.");
                        }
                        // 自動接続が指示されていた場合は、カメラとの接続処理を行う
                        connectToCamera();
                    } else {
                        if (info == null)
                        {
                            Log.v(TAG, "NETWORK INFO IS NULL.");
                        } else {
                            Log.v(TAG, "isWifiEnabled : " + wifiManager.isWifiEnabled() + " NetworkId : " + info.getNetworkId());
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, "onReceiveBroadcastOfConnection() EXCEPTION" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Wifi接続状態の監視
     * (接続の実処理は onReceiveBroadcastOfConnection() で実施)
     */
    @Override
    public void startWatchWifiStatus(Context context)
    {
        Log.v(TAG, "startWatchWifiStatus()");
        statusReceiver.onStatusNotify("prepare");

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(connectionReceiver, filter);
    }

    /**
     * Wifi接続状態の監視終了
     */
    @Override
    public void stopWatchWifiStatus(Context context)
    {
        Log.v(TAG, "stopWatchWifiStatus()");
        context.unregisterReceiver(connectionReceiver);
        disconnect(false);
    }

    /**
     * 　 カメラとの接続を解除する
     *
     *   @param powerOff 真ならカメラの電源オフを伴う
     */
    @Override
    public void disconnect(boolean powerOff)
    {
        Log.v(TAG, "disconnect()");
        disconnectFromCamera(powerOff);
        connectionStatus = CameraConnectionStatus.DISCONNECTED;
        statusReceiver.onCameraDisconnected();
    }

    /**
     * カメラとの再接続を指示する
     */
    @Override
    public void connect()
    {
        Log.v(TAG, "connect()");
        connectToCamera();
    }

    /**
     *   接続リトライのダイアログを出す
     *
     * @param message 表示用のメッセージ
     */
    @Override
    public void alertConnectingFailed(String message)
    {
        Log.v(TAG, "alertConnectingFailed() : " + message);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_title_connect_failed_sony))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.dialog_title_button_retry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        connect();
                    }
                })
                .setNeutralButton(R.string.dialog_title_button_network_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        try
                        {
                            // Wifi 設定画面を表示する
                            context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                        catch (android.content.ActivityNotFoundException ex)
                        {
                            // Activity が存在しなかった...設定画面が起動できなかった
                            Log.v(TAG, "android.content.ActivityNotFoundException...");

                            // この場合は、再試行と等価な動きとする
                            connect();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                builder.show();
            }
        });
    }

    @Override
    public CameraConnectionStatus getConnectionStatus()
    {
        Log.v(TAG, "getConnectionStatus()");
        return (connectionStatus);
    }

    @Override
    public void forceUpdateConnectionStatus(CameraConnectionStatus status)
    {
        Log.v(TAG, "forceUpdateConnectionStatus()");
        connectionStatus = status;
    }

    /**
     * カメラとの切断処理
     */
    private void disconnectFromCamera(final boolean powerOff)
    {
        Log.v(TAG, "disconnectFromCamera()");
        try
        {
            cameraExecutor.execute(new SonyCameraDisconnectSequence(powerOff));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * カメラとの接続処理
     */
    private void connectToCamera()
    {
        Log.v(TAG, "connectToCamera()");
        connectionStatus = CameraConnectionStatus.CONNECTING;
        try
        {
            cameraExecutor.execute(new SonyCameraConnectSequence(context,statusReceiver, this, cameraHolder, listener));
        }
        catch (Exception e)
        {
            Log.v(TAG, "connectToCamera() EXCEPTION : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
