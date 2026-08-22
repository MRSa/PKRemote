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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.PreferenceManager
import net.osdn.gokigen.pkremote.camera.CameraInterfaceProvider
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor
import net.osdn.gokigen.pkremote.scene.CameraSceneUpdater
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import net.osdn.gokigen.pkremote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),
    IInformationReceiver,
    ICardSlotSelector
{
    private lateinit var binding: ActivityMainBinding

    private var interfaceProvider: IInterfaceProvider? = null
    private var sceneUpdater: CameraSceneUpdater? = null
    private var slotSelectionReceiver: ICardSlotSelectionReceiver? = null

    // --- ActivityResult APIを使用
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            try {
                prepareClass()
                onReadyClass()
            }
            catch (_: Exception)
            {
                Log.e(TAG, "class initialize fail...")
            }
        } else {
            Log.v(TAG, "Permissions not granted")
            Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        Log.v(TAG, " ----- onCreate() -----")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupWindowInset(binding.container)
        setupNavigation()
        setupOnBackPressed()

        initializeClass()

        checkAndRequestPermissions()
    }

    private fun setupOnBackPressed() {
        // onBackPressed 対策
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                sceneUpdater?.updateBottomNavigationMenu()
            }
        })
    }

    private fun setupNavigation() {
        binding.navigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_photo_library -> {
                    sceneUpdater?.changeScenceToImageList()
                    true
                }
                R.id.navigation_calendar -> {
                    sceneUpdater?.changeSceneToCalendar()
                    true
                }
                R.id.navigation_auto_transfer -> {
                    sceneUpdater?.changeSceneToAutoTransfer()
                    true
                }
                R.id.navigation_settings -> {
                    sceneUpdater?.changeSceneToConfiguration()
                    true
                }
                else -> false
            }
        }
    }

    // OSバージョンに応じて必要な権限のみを取得してチェックする
    private fun checkAndRequestPermissions() {
        val requiredPermissions = getRequiredPermissions()
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (missingPermissions.isEmpty()) {
            Log.v(TAG, "allPermissionsGranted() : true")
            prepareClass()
            onReadyClass()
        } else {
            Log.v(TAG, "====== REQUEST PERMISSIONS ======")
            requestPermissionLauncher.launch(missingPermissions)
        }
    }

    private fun getRequiredPermissions(): List<String> {
        val list = mutableListOf(
            Manifest.permission.INTERNET,
            Manifest.permission.VIBRATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
        )

        // Android 12 (API 31) 以下のみ必要なストレージ権限
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // Android 10 (API 29) 以降
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            list.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }

        // Android 13 (API 33) 以降
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        // Android 17 (API 37) 以降
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            list.add(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }

        return list
    }

    private fun setupWindowInset(view: View)
    {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onPause()
    {
        super.onPause()
        try { interfaceProvider?.cameraConnection?.stopWatchWifiStatus(this) } catch (_: Exception) { }
    }

    private fun initializeClass()
    {
        sceneUpdater = CameraSceneUpdater.newInstance(this)
        sceneUpdater?.let { updater ->
            val scene = updater as ICameraStatusReceiver
            val provider = CameraInterfaceProvider.newInstance(this, scene, this, this)
            interfaceProvider = provider
            updater.changeFirstFragment(provider)
        }
    }

    private fun prepareClass()
    {
        Log.v(TAG, "prepareClass()")

        binding.buttonWifiConnect.setOnClickListener {
            sceneUpdater?.changeCameraConnection()
            vibrate()
        }

        binding.buttonReload.setOnClickListener {
            sceneUpdater?.reloadRemoteImageContents()
            vibrate()
        }

        val isPanasonic = interfaceProvider?.cammeraConnectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC
        setupCardSlotSelection(isPanasonic)
    }

    private fun setupCardSlotSelection(isEnabled: Boolean) {
        if (isEnabled)
        {
            binding.cardSlotSelection.visibility = View.VISIBLE
            val adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sd_card_slot,
                android.R.layout.simple_spinner_item
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.cardSlotSelection.adapter = adapter
            binding.cardSlotSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val item = parent.getItemAtPosition(position) as String
                    slotSelectionReceiver?.slotSelected(item)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        else
        {
            binding.cardSlotSelection.visibility = View.GONE
        }
    }

    private fun onReadyClass() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isAutoConnectCamera = preferences.getBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true)
        Log.v(TAG, "isAutoConnectCamera() : $isAutoConnectCamera")

        if (isAutoConnectCamera) {
            sceneUpdater?.changeCameraConnection()
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) {
            Log.v(TAG, "No Vibrator available.")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    override fun updateMessage(message: String, isBold: Boolean, isColor: Boolean, color: Int) {
        runOnUiThread {
            binding.message.text = message
            binding.message.typeface = if (isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            binding.message.setTextColor(if (isColor) color else Color.DKGRAY)
        }
    }

    override fun setupSlotSelector(isEnable: Boolean, slotSelectionReceiver: ICardSlotSelectionReceiver?) {
        Log.v(TAG, "setupSlotSelector $isEnable")
        this.slotSelectionReceiver = slotSelectionReceiver
        runOnUiThread { setupCardSlotSelection(isEnable) }
    }

    override fun selectSlot(slotId: String) {
        Log.v(TAG, "selectSlot : $slotId")
    }

    override fun changedCardSlot(slotId: String) {
        Log.v(TAG, "changedCardSlot : $slotId")
        sceneUpdater?.reloadRemoteImageContents()
        vibrate()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
