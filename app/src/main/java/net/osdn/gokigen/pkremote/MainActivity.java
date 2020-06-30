package net.osdn.gokigen.pkremote;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import net.osdn.gokigen.pkremote.camera.CameraInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;
import net.osdn.gokigen.pkremote.scene.CameraSceneUpdater;

/**
 *
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, IInformationReceiver, ICardSlotSelector, AdapterView.OnItemSelectedListener
{
    private final String TAG = toString();
    private IInterfaceProvider interfaceProvider = null;
    private CameraSceneUpdater scenceUpdater = null;

    private ImageButton mImageConnectButton = null;
    private ImageButton mReloadButton = null;
    private Spinner mCardSlotSelection = null;
    private ICardSlotSelectionReceiver slotSelectionReceiver = null;
    //private TextView mTextMessage = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_photo_library:
                    scenceUpdater.changeScenceToImageList();
                    return (true);
                case R.id.navigation_calendar:
                    scenceUpdater.changeSceneToCalendar();
                    return (true);
                case R.id.navigation_auto_transfer:
                    scenceUpdater.changeSceneToAutoTransfer();
                    return (true);
                case R.id.navigation_settings:
                    scenceUpdater.changeSceneToConfiguration();
                    return (true);
            }
            return (false);
        }
    };

    /**
     *
     *
     */
    @Override
    public void onBackPressed()
    {
        //Log.v(TAG, "onBackPressed()");
        super.onBackPressed();
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                if (scenceUpdater != null)
                {
                    scenceUpdater.updateBottomNavigationMenu();
                }
            }
        });
    }

    /**
     *
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try
        {
            ActionBar bar = getSupportActionBar();
            if (bar != null)
            {
                // タイトルバーは表示しない
                bar.hide();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //mTextMessage = findViewById(R.id.message);
        mImageConnectButton = findViewById(R.id.button_wifi_connect);
        mReloadButton = findViewById(R.id.button_reload);
        mCardSlotSelection = findViewById(R.id.card_slot_selection);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // パーミッション群のオプトイン
        final int REQUEST_NEED_PERMISSIONS = 1010;

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_MEDIA_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET,
                    },
                    REQUEST_NEED_PERMISSIONS);
        }
        initializeClass();
        prepareClass();
        onReadyClass();
    }

    /**
     * パーミッション設定が終わった後...
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        prepareClass();
        onReadyClass();
    }

    /**
     *
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        try
        {
            ICameraConnection connection = interfaceProvider.getCameraConnection();
            if (connection != null)
            {
                connection.stopWatchWifiStatus(this);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * クラスの初期化 (instantiate)
     */
    private void initializeClass()
    {
        try
        {
            scenceUpdater = CameraSceneUpdater.newInstance(this);
            interfaceProvider = CameraInterfaceProvider.newInstance(this, scenceUpdater, this, this);
            scenceUpdater.changeFirstFragment(interfaceProvider);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 本クラスの準備
     */
    private void prepareClass()
    {
        try
        {
            mImageConnectButton.setOnClickListener(this);
            mReloadButton.setOnClickListener(this);
            setupCardSlotSelection(interfaceProvider.getCammeraConnectionMethod() == ICameraConnection.CameraConnectionMethod.PANASONIC);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setupCardSlotSelection(boolean isEnabled)
    {
        try
        {
            if (mCardSlotSelection == null)
            {
                mCardSlotSelection = findViewById(R.id.card_slot_selection);
            }
            if (isEnabled)
            {
                // 接続モードが Panasonic の時だけ、SD Card 選択を出せるようにする
                mCardSlotSelection.setVisibility(View.VISIBLE);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sd_card_slot, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mCardSlotSelection.setAdapter(adapter);
                mCardSlotSelection.setOnItemSelectedListener(this);
            }
            else
            {
                mCardSlotSelection.setVisibility(View.GONE);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    /**
     * 初期化終了時の処理 (カメラへの自動接続)
     */
    private void onReadyClass()
    {
        try
        {
            // カメラに自動接続するかどうか確認
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isAutoConnectCamera = preferences.getBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true);
            Log.v(TAG, "isAutoConnectCamera() : " + isAutoConnectCamera);

            // カメラに接続する
            if (isAutoConnectCamera)
            {
                // 自動接続の指示があったとき
                scenceUpdater.changeCameraConnection();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v)
    {
        try
        {
            int id = v.getId();
            switch (id)
            {
                case R.id.button_wifi_connect:
                    // カメラとの接続を行う
                    scenceUpdater.changeCameraConnection();
                    vibrate();
                    break;

                case R.id.button_reload:
                    // 画像一覧情報をリロードする
                    scenceUpdater.reloadRemoteImageContents();
                    vibrate();
                    break;

                default:
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    private void vibrate()
    {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null)
            {
                vibrator.vibrate(50);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void updateMessage(final String message, final boolean isBold, final boolean isColor,  final int color)
    {
        try {
            final TextView messageArea = findViewById(R.id.message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if ((messageArea != null) && (message != null))
                        {
                            messageArea.setText(message);
                            if (isBold)
                            {
                                messageArea.setTypeface(Typeface.DEFAULT_BOLD);
                            }
                            if (isColor)
                            {
                                messageArea.setTextColor(color);
                            }
                            else
                            {
                                messageArea.setTextColor(Color.DKGRAY);
                            }
                            messageArea.invalidate();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        try
        {
            String item = (String) parent.getItemAtPosition(position);
            Log.v(TAG, " onItemSelected : " +  item);
            if (slotSelectionReceiver != null)
            {
                slotSelectionReceiver.slotSelected(item);
            }
            vibrate();

            // scenceUpdater.reloadRemoteImageContents();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        // 何もしない
    }

    @Override
    public void setupSlotSelector(final boolean isEnable, @Nullable ICardSlotSelectionReceiver slotSelectionReceiver)
    {
        try
        {
            Log.v(TAG, "  ------- setupSlotSelector " + isEnable);
            this.slotSelectionReceiver = slotSelectionReceiver;
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    setupCardSlotSelection(isEnable);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
