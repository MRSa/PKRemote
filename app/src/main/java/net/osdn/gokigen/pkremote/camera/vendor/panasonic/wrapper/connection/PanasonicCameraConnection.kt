package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.connection

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection.CameraConnectionStatus
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCameraHolder
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class PanasonicCameraConnection(
    private val context: Activity,
    private val statusReceiver: ICameraStatusReceiver,
    private val cameraHolder: IPanasonicCameraHolder,
    private val listener: ICameraChangeListener
) : ICameraConnection
{
    private val connectionReceiver: BroadcastReceiver
    private val cameraExecutor: Executor = Executors.newFixedThreadPool(1)
    private var connectionStatus = CameraConnectionStatus.UNKNOWN

    init
    {
        Log.v(TAG, "PanasonicCameraConnection()")
        connectionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent)
            {
                onReceiveBroadcastOfConnection(context, intent)
            }
        }
    }

    private fun onReceiveBroadcastOfConnection(context: Context, intent: Intent)
    {
        statusReceiver.onStatusNotify(context.getString(R.string.connect_check_wifi))
        Log.v(TAG, context.getString(R.string.connect_check_wifi))
        val action = intent.action
        if (action == null)
        {
            Log.v(TAG, "intent.getAction() : null")
            return
        }

        try
        {
            @Suppress("DEPRECATION")
            if (action == ConnectivityManager.CONNECTIVITY_ACTION)
            {
                Log.v(TAG, "onReceiveBroadcastOfConnection() : CONNECTIVITY_ACTION")

                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val info = wifiManager.connectionInfo
                if (wifiManager.isWifiEnabled && info != null)
                {
                    if (info.networkId != -1)
                    {
                        Log.v(TAG, "Network ID is -1, there is no currently connected network.")
                    }
                    // 自動接続が指示されていた場合は、カメラとの接続処理を行う
                    connectToCamera()
                }
                else
                {
                    if (info == null)
                    {
                        Log.v(TAG, "NETWORK INFO IS NULL.")
                    }
                    else
                    {
                        Log.v(TAG, "isWifiEnabled : " + wifiManager.isWifiEnabled + " NetworkId : " + info.networkId)
                    }
                }
            }
        }
        catch (e: Exception)
        {
            Log.w(TAG, "onReceiveBroadcastOfConnection() EXCEPTION" + e.message)
            e.printStackTrace()
        }
    }

    /**
     * Wifi接続状態の監視
     * (接続の実処理は onReceiveBroadcastOfConnection() で実施)
     */
    override fun startWatchWifiStatus(context: Context)
    {
        try
        {
            val filter = IntentFilter()
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            @Suppress("DEPRECATION")
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(connectionReceiver, filter)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        Log.v(TAG, "startWatchWifiStatus()")
        statusReceiver.onStatusNotify("prepare")
    }

    /**
     * Wifi接続状態の監視終了
     */
    override fun stopWatchWifiStatus(context: Context)
    {
        try
        {
            Log.v(TAG, "stopWatchWifiStatus()")
            context.unregisterReceiver(connectionReceiver)
            disconnect(false)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 　 カメラとの接続を解除する
     *
     * @param powerOff 真ならカメラの電源オフを伴う
     */
    override fun disconnect(powerOff: Boolean)
    {
        try
        {
            Log.v(TAG, "disconnect()")
            disconnectFromCamera(powerOff)
            connectionStatus = CameraConnectionStatus.DISCONNECTED
            statusReceiver.onCameraDisconnected()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * カメラとの再接続を指示する
     */
    override fun connect()
    {
        try
        {
            Log.v(TAG, "connect()")
            connectToCamera()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 接続リトライのダイアログを出す
     *
     * @param message 表示用のメッセージ
     */
    override fun alertConnectingFailed(message: String)
    {
        try
        {
            Log.v(TAG, "alertConnectingFailed() : $message")

            context.runOnUiThread {
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.dialog_title_connect_failed_panasonic))
                    .setMessage(message)
                    .setPositiveButton(context.getString(R.string.dialog_title_button_retry)) { _, _ -> connect() }
                    .setNeutralButton(R.string.dialog_title_button_network_settings) { _, _ ->
                        try
                        {
                            // Wifi 設定画面を表示する
                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                        }
                        catch (ex: ActivityNotFoundException)
                        {
                            // Activity が存在しなかった...設定画面が起動できなかった
                            Log.v(TAG, "android.content.ActivityNotFoundException...")

                            // この場合は、再試行と等価な動きとする
                            connect()
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                    .show()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun getConnectionStatus(): CameraConnectionStatus
    {
        Log.v(TAG, "getConnectionStatus()")
        return (connectionStatus)
    }

    override fun forceUpdateConnectionStatus(status: CameraConnectionStatus)
    {
        Log.v(TAG, "forceUpdateConnectionStatus()")
        connectionStatus = status
    }

    /**
     * カメラとの切断処理
     */
    private fun disconnectFromCamera(powerOff: Boolean)
    {
        try
        {
            Log.v(TAG, "disconnectFromCamera()")
            cameraExecutor.execute(PanasonicCameraDisconnectSequence(powerOff))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * カメラとの接続処理
     */
    private fun connectToCamera()
    {
        Log.v(TAG, "connectToCamera()")
        connectionStatus = CameraConnectionStatus.CONNECTING
        try
        {
            cameraExecutor.execute(PanasonicCameraConnectSequence(context, statusReceiver, this, cameraHolder, listener))
        }
        catch (e: Exception)
        {
            Log.v(TAG, "connectToCamera() EXCEPTION : " + e.message)
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = PanasonicCameraConnection::class.java.simpleName
    }
}