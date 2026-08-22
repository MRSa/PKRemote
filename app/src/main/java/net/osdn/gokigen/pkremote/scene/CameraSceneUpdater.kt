package net.osdn.gokigen.pkremote.scene

import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.calendar.CalendarFragment
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection.CameraConnectionMethod
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection.CameraConnectionStatus
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentsRecognizer.ICameraContentsListCallback
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver
import net.osdn.gokigen.pkremote.camera.vendor.sony.cameraproperty.SonyCameraApiListFragment
import net.osdn.gokigen.pkremote.logcat.LogCatFragment
import net.osdn.gokigen.pkremote.playback.ImageGridViewFragment
import net.osdn.gokigen.pkremote.preference.canon.CanonPreferenceFragment
import net.osdn.gokigen.pkremote.preference.fujix.FujiXPreferenceFragment
import net.osdn.gokigen.pkremote.preference.nikon.NikonPreferenceFragment
import net.osdn.gokigen.pkremote.preference.olympus.OpcPreferenceFragment
import net.osdn.gokigen.pkremote.preference.olympuspen.OlympusPenPreferenceFragment
import net.osdn.gokigen.pkremote.preference.panasonic.PanasonicPreferenceFragment
import net.osdn.gokigen.pkremote.preference.pixpro.PixproPreferenceFragment
import net.osdn.gokigen.pkremote.preference.ricohgr2.RicohGr2PreferenceFragment
import net.osdn.gokigen.pkremote.preference.sony.SonyPreferenceFragment
import net.osdn.gokigen.pkremote.preference.theta.ThetaPreferenceFragment
import net.osdn.gokigen.pkremote.preference.visionkids.VisionKidsPreferenceFragment.Companion.newInstance
import net.osdn.gokigen.pkremote.transfer.AutoTransferFragment

class CameraSceneUpdater private constructor(private val activity: AppCompatActivity) :
    ICameraStatusReceiver, IChangeScene, ICameraContentsListCallback {
    private val TAG = toString()
    private val bottomNavigationView: BottomNavigationView?
    private var interfaceProvider: IInterfaceProvider? = null
    private var anotherStatusReceiver: ICameraStatusReceiver? = null
    private var preferenceFragment: PreferenceFragmentCompat? = null
    private var logCatFragment: LogCatFragment? = null
    private var calendarFragment: CalendarFragment? = null
    private var gridViewFragment: ImageGridViewFragment? = null
    private var autoTransferFragment: AutoTransferFragment? = null
    private var sonyApiListFragmentSony: SonyCameraApiListFragment? = null

    // コンストラクタ
    init {
        this.bottomNavigationView = activity.findViewById<BottomNavigationView?>(R.id.navigation)
    }

    // 一番最初のフラグメントを表示する
    fun changeFirstFragment(interfaceProvider: IInterfaceProvider) {
        this.interfaceProvider = interfaceProvider
        try {
            bottomNavigationView!!.setSelectedItemId(R.id.navigation_calendar)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 初期画面(カレンダー画面)へ遷移
        changeSceneToCalendar()
    }

    // ICameraStatusReceiver
    override fun onStatusNotify(message: String?) {
        Log.v(TAG, " CONNECTION MESSAGE : " + message)
        try {
            if (anotherStatusReceiver != null) {
                anotherStatusReceiver!!.onStatusNotify(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ICameraStatusReceiver
    override fun onCameraConnected() {
        Log.v(TAG, "onCameraConnected()")
        updateConnectionStatus(
            activity.getString(R.string.connect_connected),
            CameraConnectionStatus.CONNECTED
        )
        try {
            val connection = getCameraConnection(interfaceProvider!!.getCammeraConnectionMethod())
            if (connection != null) {
                connection.forceUpdateConnectionStatus(CameraConnectionStatus.CONNECTED)
            }
            if (anotherStatusReceiver != null) {
                anotherStatusReceiver!!.onCameraConnected()
            }
            val recognizer = interfaceProvider!!.getCameraContentsRecognizer()
            if (recognizer != null) {
                // カメラ内のコンテンツ一覧を作成するように指示する
                recognizer.getRemoteCameraContentsList(true, this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ICameraStatusReceiver
    override fun onCameraDisconnected() {
        Log.v(TAG, "onCameraDisconnected()")
        val message = activity.getString(R.string.camera_disconnected)
        updateConnectionStatus(message, CameraConnectionStatus.DISCONNECTED)
        try {
            if (anotherStatusReceiver != null) {
                anotherStatusReceiver!!.onCameraDisconnected()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ICameraStatusReceiver
    override fun onCameraOccursException(message: String?, e: Exception) {
        Log.v(TAG, "onCameraOccursException() " + message)
        try {
            val connectionStatus: CameraConnectionStatus // = ICameraConnection.CameraConnectionStatus.UNKNOWN;

            e.printStackTrace()
            val connection = getCameraConnection(interfaceProvider!!.getCammeraConnectionMethod())
            if (connection != null) {
                connectionStatus = connection.getConnectionStatus()
                connection.alertConnectingFailed(message + " " + e.getLocalizedMessage())
                updateConnectionStatus(message, connectionStatus)
            }
            if (anotherStatusReceiver != null) {
                anotherStatusReceiver!!.onCameraOccursException(message, e)
            }
        } catch (ee: Exception) {
            ee.printStackTrace()
        }
    }

    /**
     * カメラとの接続状態を表示更新する
     */
    private fun updateConnectionStatus(message: String?, status: CameraConnectionStatus) {
        try {
            val resId: Int
            when (status) {
                CameraConnectionStatus.CONNECTED -> resId = R.drawable.ic_cloud_done_black_24dp
                CameraConnectionStatus.CONNECTING -> resId = R.drawable.ic_cloud_queue_black_24dp
                CameraConnectionStatus.DISCONNECTED -> resId = R.drawable.ic_cloud_off_black_24dp
                CameraConnectionStatus.UNKNOWN -> resId = R.drawable.ic_cloud_queue_grey_24dp
                else -> resId = R.drawable.ic_cloud_queue_grey_24dp
            }
            val messageArea = activity.findViewById<TextView?>(R.id.message)
            val buttonArea = activity.findViewById<ImageButton?>(R.id.button_wifi_connect)
            activity.runOnUiThread(object : Runnable {
                override fun run() {
                    try {
                        if (buttonArea != null) {
                            buttonArea.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    activity.getResources(),
                                    resId,
                                    null
                                )
                            )
                            buttonArea.invalidate()
                        }
                        if ((messageArea != null) && (message != null)) {
                            messageArea.setText(message)
                            messageArea.invalidate()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //  IChangeScene
    override fun changeSceneToCameraPropertyList() {
    }

    //  IChangeScene
    override fun changeSceneToConfiguration() {
        try {
            if (preferenceFragment == null) {
                try {
                    //preferenceFragment = RicohGr2PreferenceFragment.newInstance(activity, this);
                    val connectionMethod = interfaceProvider!!.getCammeraConnectionMethod()
                    if (connectionMethod == CameraConnectionMethod.RICOH) {
                        preferenceFragment = RicohGr2PreferenceFragment.newInstance(activity, this)
                    } else if (connectionMethod == CameraConnectionMethod.FUJI_X) {
                        preferenceFragment = FujiXPreferenceFragment.newInstance(activity, this)
                        //} else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY) {
                        //    preferenceFragment = SonyPreferenceFragment.newInstance(this, this);
                    } else if (connectionMethod == CameraConnectionMethod.PANASONIC) {
                        preferenceFragment = PanasonicPreferenceFragment.newInstance(activity, this)
                    } else if (connectionMethod == CameraConnectionMethod.SONY) {
                        preferenceFragment = SonyPreferenceFragment.newInstance(activity, this)
                    } else if (connectionMethod == CameraConnectionMethod.CANON) {
                        preferenceFragment = CanonPreferenceFragment.newInstance(activity, this)
                    } else if (connectionMethod == CameraConnectionMethod.NIKON) {
                        preferenceFragment = NikonPreferenceFragment.newInstance(activity, this)
                    } else if (connectionMethod == CameraConnectionMethod.OLYMPUS) {
                        preferenceFragment =
                            OlympusPenPreferenceFragment.newInstance(activity, this)
                    } else if (connectionMethod == CameraConnectionMethod.THETA) {
                        preferenceFragment = ThetaPreferenceFragment.newInstance(activity, this)
                    } else if (connectionMethod == CameraConnectionMethod.PIXPRO) {
                        preferenceFragment = PixproPreferenceFragment.newInstance(activity, this)
                    } else if (connectionMethod == CameraConnectionMethod.VISIONKIDS) {
                        preferenceFragment = newInstance(activity, this)
                    } else  //  if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
                    {
                        preferenceFragment =
                            OpcPreferenceFragment.newInstance(activity, interfaceProvider!!, this)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    //preferenceFragment = SonyPreferenceFragment.newInstance(this, this);
                }
            }
            val transaction = activity.getSupportFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment1, preferenceFragment!!)
            // backstackに追加
            transaction.addToBackStack(null)
            transaction.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //  IChangeScene
    override fun changeCameraConnection() {
        if (interfaceProvider == null) {
            Log.v(TAG, "changeCameraConnection() : interfaceProvider is NULL")
            return
        }
        try {
            interfaceProvider!!.resetCameraConnectionMethod()
            val connection = interfaceProvider!!.getCameraConnection()
            if (connection != null) {
                val status = connection.getConnectionStatus()
                if (status == CameraConnectionStatus.CONNECTED) {
                    // 接続中のときには切断する
                    connection.disconnect(false)
                    return
                }
                // 接続中でない時は、接続中にする
                connection.startWatchWifiStatus(activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //  IChangeScene
    override fun reloadRemoteImageContents() {
        try {
            val recognizer = interfaceProvider!!.getCameraContentsRecognizer()
            if (recognizer != null) {
                // カメラ内のコンテンツ一覧を作成するように指示する
                recognizer.getRemoteCameraContentsList(true, this)
            }
            if (gridViewFragment != null) {
                // サムネイル画像のキャッシュをクリアする
                gridViewFragment!!.clearImageCache()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //  IChangeScene
    override fun changeSceneToDebugInformation() {
        if (logCatFragment == null) {
            logCatFragment = LogCatFragment.newInstance()
        }
        val transaction = activity.getSupportFragmentManager().beginTransaction()
        transaction.replace(R.id.fragment1, logCatFragment!!)
        // backstackに追加
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //  IChangeScene
    override fun changeSceneToApiList() {
        if (sonyApiListFragmentSony == null) {
            sonyApiListFragmentSony = SonyCameraApiListFragment.newInstance(interfaceProvider!!)
        }
        val transaction = activity.getSupportFragmentManager().beginTransaction()
        transaction.replace(R.id.fragment1, sonyApiListFragmentSony!!)
        // backstackに追加
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //  IChangeScene
    override fun changeSceneToAutoTransfer() {
        if (autoTransferFragment == null) {
            autoTransferFragment = AutoTransferFragment.newInstance(activity, interfaceProvider!!)
        }
        val transaction = activity.getSupportFragmentManager().beginTransaction()
        transaction.replace(R.id.fragment1, autoTransferFragment!!)
        // backstackに追加
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //  IChangeScene
    override fun changeSceneToCalendar() {
        if (calendarFragment == null) {
            calendarFragment = CalendarFragment.newInstance(activity, this, interfaceProvider!!)
        }
        val transaction = activity.getSupportFragmentManager().beginTransaction()
        transaction.replace(R.id.fragment1, calendarFragment!!)
        // backstackに追加
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //  IChangeScene
    override fun changeScenceDateSelected(filterLabel: String?) {
        Log.v(TAG, "changeScenceDateSelected()")

        bottomNavigationView!!.setSelectedItemId(R.id.navigation_photo_library)
        changeScenceToImageList(filterLabel)
    }

    //  IChangeScene
    // 画像一覧画面を開く
    override fun changeScenceToImageList() {
        changeScenceToImageList(null)
    }

    private fun changeScenceToImageList(filterLabel: String?) {
        Log.v(TAG, "changeScenceToImageList() : " + filterLabel)
        try {
            if (gridViewFragment == null) {
                gridViewFragment = ImageGridViewFragment.newInstance(interfaceProvider!!)
            }
            gridViewFragment!!.setFilterLabel(filterLabel)
            val transaction = activity.getSupportFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment1, gridViewFragment!!)
            // backstackに追加
            transaction.addToBackStack(null)
            transaction.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //  IChangeScene
    override fun exitApplication() {
        Log.v(TAG, "exitApplication()")
        try {
            val connection = getCameraConnection(interfaceProvider!!.getCammeraConnectionMethod())
            if (connection != null) {
                connection.disconnect(true)
            }
            activity.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun updateBottomNavigationMenu() {
        try {
            // ナビゲーション部分の選択状態をしたい...
            var changeId = 0
            if (calendarFragment!!.isFragmentActive) {
                changeId = R.id.navigation_calendar
            } else if (gridViewFragment!!.isFragmentActive()) {
                changeId = R.id.navigation_photo_library
            }
            if ((bottomNavigationView != null) && (changeId != 0)) {
                bottomNavigationView.setSelectedItemId(changeId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setAnotherStatusReceiver(statusReceiver: ICameraStatusReceiver?) {
        this.anotherStatusReceiver = statusReceiver
    }

    private fun getCameraConnection(method: CameraConnectionMethod?): ICameraConnection? {
        Log.v(TAG, "method : " + method)
        return (interfaceProvider!!.getCameraConnection())
    }

    override fun contentsListCreated(nofContents: Int) {
        Log.v(TAG, "contentsListCreated() : " + nofContents)

        // カレンダー画面のリフレッシュを行いたい (かなり無理やり...)
        if ((calendarFragment != null) && (calendarFragment!!.isFragmentActive)) {
            calendarFragment!!.contentsListCreated(nofContents)
        }
    }

    companion object {
        fun newInstance(activity: AppCompatActivity): CameraSceneUpdater {
            return (CameraSceneUpdater(activity))
        }
    }
}
