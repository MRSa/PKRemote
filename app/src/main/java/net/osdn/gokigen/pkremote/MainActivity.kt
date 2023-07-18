package net.osdn.gokigen.pkremote

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationBarView
import net.osdn.gokigen.pkremote.camera.CameraInterfaceProvider
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor
import net.osdn.gokigen.pkremote.scene.CameraSceneUpdater

/**
 *
 *
 */
class MainActivity : AppCompatActivity(),
    View.OnClickListener,
    IInformationReceiver,
    ICardSlotSelector,
    AdapterView.OnItemSelectedListener
{
    private var interfaceProvider: IInterfaceProvider? = null
    private var sceneUpdater: CameraSceneUpdater? = null
    private var mImageConnectButton: ImageButton? = null
    private var mReloadButton: ImageButton? = null
    private var mCardSlotSelection: Spinner? = null
    private var slotSelectionReceiver: ICardSlotSelectionReceiver? = null

    private val mOnNavigationItemSelectedListener =
        NavigationBarView.OnItemSelectedListener { item ->
        //BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_photo_library -> {
                    sceneUpdater?.changeScenceToImageList()
                    return@OnItemSelectedListener true
                }

                R.id.navigation_calendar -> {
                    sceneUpdater?.changeSceneToCalendar()
                    return@OnItemSelectedListener true
                }

                R.id.navigation_auto_transfer -> {
                    sceneUpdater?.changeSceneToAutoTransfer()
                    return@OnItemSelectedListener true
                }

                R.id.navigation_settings -> {
                    sceneUpdater?.changeSceneToConfiguration()
                    return@OnItemSelectedListener true
                }
            }
            false
        }

    /**
     *
     *
     */
    override fun onBackPressed()
    {
        //Log.v(TAG, "onBackPressed()");
        super.onBackPressed()
        runOnUiThread { sceneUpdater?.updateBottomNavigationMenu() }
    }

    /**
     *
     *
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        ///////// SHOW SPLASH SCREEN /////////
        //installSplashScreen()
        Log.v(TAG, " ----- onCreate() -----")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        try
        {
            mImageConnectButton = findViewById(R.id.button_wifi_connect)
            mReloadButton = findViewById(R.id.button_reload)
            mCardSlotSelection = findViewById(R.id.card_slot_selection)
            //val navigation = findViewById<BottomNavigationView>(R.id.navigation)
            //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
            val navigation = findViewById<NavigationBarView>(R.id.navigation)
            navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        try
        {
            initializeClass()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        if (allPermissionsGranted())
        {
            Log.v(TAG, "allPermissionsGranted() : true")
            prepareClass()
            onReadyClass()
        }
        else
        {
            Log.v(TAG, "====== REQUEST PERMISSIONS ======")
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun allPermissionsGranted() : Boolean
    {
        var result = true
        for (param in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    param
                ) != PackageManager.PERMISSION_GRANTED
            )
            {
                // Permission Denied
                if ((param == Manifest.permission.READ_EXTERNAL_STORAGE)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN))
                {
                    // この場合は権限付与の判断を除外 (デバイスが JELLY_BEAN よりも古く、READ_EXTERNAL_STORAGE がない場合）
                }
                else if ((param == Manifest.permission.ACCESS_MEDIA_LOCATION)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q))
                {
                    //　この場合は権限付与の判断を除外 (デバイスが (10) よりも古く、ACCESS_MEDIA_LOCATION がない場合）
                }
                else
                {
                    result = false
                }
            }
        }
        return (result)
    }

/*
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
*/


    /**
     * パーミッション設定が終わった後...
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.v(TAG, "------------------------- onRequestPermissionsResult() ")
        if (requestCode == REQUEST_CODE_PERMISSIONS)
        {
            if (allPermissionsGranted())
            {
                prepareClass()
                onReadyClass()
            }
            else
            {
                Log.v(TAG, "----- onRequestPermissionsResult() : false")
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                //Snackbar.make(main_layout,"Permissions not granted by the user.", Snackbar.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     *
     */
    override fun onPause()
    {
        super.onPause()
        try
        {
            val connection = interfaceProvider?.cameraConnection
            connection?.stopWatchWifiStatus(this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * クラスの初期化 (instantiate)
     */
    private fun initializeClass()
    {
        try
        {
            sceneUpdater = CameraSceneUpdater.newInstance(this)
            if (sceneUpdater != null)
            {
                val scene : ICameraStatusReceiver = sceneUpdater as CameraSceneUpdater
                interfaceProvider = CameraInterfaceProvider.newInstance(this, scene, this, this)
                val provider = interfaceProvider as IInterfaceProvider
                sceneUpdater?.changeFirstFragment(provider)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 本クラスの準備
     */
    private fun prepareClass()
    {
        try
        {
            Log.v(TAG, "prepareClass()")
            mImageConnectButton?.setOnClickListener(this)
            mReloadButton?.setOnClickListener(this)
            setupCardSlotSelection(interfaceProvider?.cammeraConnectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setupCardSlotSelection(isEnabled: Boolean)
    {
        try
        {
            if (mCardSlotSelection == null)
            {
                mCardSlotSelection = findViewById(R.id.card_slot_selection)
            }
            if (isEnabled)
            {
                // 接続モードが Panasonic の時だけ、SD Card 選択を出せるようにする
                mCardSlotSelection?.visibility = View.VISIBLE
                val adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.sd_card_slot,
                    android.R.layout.simple_spinner_item
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                mCardSlotSelection?.adapter = adapter
                mCardSlotSelection?.onItemSelectedListener = this
            }
            else
            {
                mCardSlotSelection?.visibility = View.GONE
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 初期化終了時の処理 (カメラへの自動接続)
     */
    private fun onReadyClass()
    {
        try
        {
            // カメラに自動接続するかどうか確認
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val isAutoConnectCamera =
                preferences.getBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true)
            Log.v(TAG, "isAutoConnectCamera() : $isAutoConnectCamera")

            // カメラに接続する
            if (isAutoConnectCamera)
            {
                // 自動接続の指示があったとき
                sceneUpdater?.changeCameraConnection()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View)
    {
        try
        {
            when (v.id)
            {
                R.id.button_wifi_connect -> {
                    // カメラとの接続を行う
                    sceneUpdater?.changeCameraConnection()
                    vibrate()
                }

                R.id.button_reload -> {
                    // 画像一覧情報をリロードする
                    sceneUpdater?.reloadRemoteImageContents()
                    vibrate()
                }

                else -> {}
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     *
     */
    private fun vibrate()
    {
        try
        {
            // バイブレータをつかまえる
            val vibrator  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                val vibratorManager =  this.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            }
            else
            {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            if (!vibrator.hasVibrator())
            {
                // バイブレータが搭載されていないとき...
                Log.v(TAG, " not have Vibrator...")
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            else
            {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
         }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun updateMessage(message: String, isBold: Boolean, isColor: Boolean, color: Int)
    {
        try
        {
            val messageArea = findViewById<TextView>(R.id.message)
            runOnUiThread {
                try
                {
                    messageArea.text = message
                    if (isBold) {
                        messageArea.typeface = Typeface.DEFAULT_BOLD
                    }
                    if (isColor) {
                        messageArea.setTextColor(color)
                    } else {
                        messageArea.setTextColor(Color.DKGRAY)
                    }
                    messageArea.invalidate()
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long)
    {
        try
        {
            val item = parent.getItemAtPosition(position) as String
            slotSelectionReceiver?.slotSelected(item)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?)
    {
        // 何もしない
    }

    override fun setupSlotSelector(
        isEnable: Boolean,
        slotSelectionReceiver: ICardSlotSelectionReceiver?
    )
    {
        try
        {
            Log.v(TAG, "  ------- setupSlotSelector $isEnable")
            this.slotSelectionReceiver = slotSelectionReceiver
            runOnUiThread { setupCardSlotSelection(isEnable) }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun selectSlot(slotId: String)
    {
        try
        {
            Log.v(TAG, " selectSlot : $slotId")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun changedCardSlot(slotId: String)
    {
        try
        {
            Log.v(TAG, " changedCardSlot : $slotId")
            sceneUpdater?.reloadRemoteImageContents()
            vibrate()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = MainActivity::class.java.simpleName

        private const val REQUEST_CODE_PERMISSIONS = 10
        //const val REQUEST_CODE_MEDIA_EDIT = 12
        //const val REQUEST_CODE_OPEN_DOCUMENT_TREE = 20

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.VIBRATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            //Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        )
    }
}
