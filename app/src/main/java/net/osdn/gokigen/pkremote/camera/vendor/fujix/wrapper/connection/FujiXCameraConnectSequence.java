package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.connection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.IFujiXInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXMessages;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.QueryCameraCapabilities;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.StatusRequestMessage;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.CameraRemoteMessage;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.RegistrationMessage;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.StartMessage;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.StartMessage2nd;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.StartMessage3rd;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.StartMessage4th;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.StartMessage5th;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

public class FujiXCameraConnectSequence implements Runnable, IFujiXCommandCallback, IFujiXMessages
{
    private final String TAG = this.toString();


    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final IFujiXInterfaceProvider interfaceProvider;
    private final IFujiXCommandPublisher commandIssuer;
    private boolean isBothLiveView = false;

    FujiXCameraConnectSequence(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection, @NonNull IFujiXInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, "FujiXCameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
        this.interfaceProvider = interfaceProvider;
        this.commandIssuer = interfaceProvider.getCommandPublisher();
    }

    @Override
    public void run()
    {
        try
        {
            try
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                isBothLiveView = preferences.getBoolean(IPreferencePropertyAccessor.FUJIX_DISPLAY_CAMERA_VIEW, false);
            }
            catch (Exception e)
            {
                //isBothLiveView = false;
                e.printStackTrace();
            }

            // カメラとTCP接続
            IFujiXCommandPublisher issuer = interfaceProvider.getCommandPublisher();
            if (!issuer.isConnected())
            {
                if (!interfaceProvider.getCommandCommunication().connect())
                {
                    // 接続失敗...
                    onConnectError(context.getString(R.string.dialog_title_connect_failed));
                    return;
                }
            }
            // コマンドタスクの実行開始
            issuer.start();

            // 接続シーケンスの開始
            sendRegistrationMessage();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            onConnectError(e.getLocalizedMessage());
        }
    }

    private void onConnectError(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        //Log.v(TAG, "receivedMessage : " + id + "[" + rx_body.length + " bytes]");
        //int bodyLength = 0;
        switch (id)
        {
            case SEQ_REGISTRATION:
                if (checkRegistrationMessage(rx_body))
                {
                    commandIssuer.enqueueCommand(new StartMessage(this));
                }
                break;

            case SEQ_START:
                commandIssuer.enqueueCommand(new StartMessage2nd(this));
                break;

            case SEQ_START_2ND:
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting));
                if (rx_body.length == (int)rx_body[0])
                {
                    // なぜかもうちょっとデータが飛んでくるので待つ
                    //commandIssuer.enqueueCommand(new ReceiveOnly(this));

                    commandIssuer.enqueueCommand(new StartMessage3rd(this));
                }
                else
                {
                    commandIssuer.enqueueCommand(new StartMessage3rd(this));
                }
                break;

            case SEQ_START_2ND_RECEIVE:
                commandIssuer.enqueueCommand(new StartMessage3rd(this));
                break;

            case SEQ_START_3RD:
                commandIssuer.enqueueCommand(new StartMessage4th(this));
                break;

            case SEQ_START_4TH:
                if (isBothLiveView)
                {
                    // カメラのLCDと遠隔のライブビューを同時に表示する場合...
                    commandIssuer.enqueueCommand(new CameraRemoteMessage(this));
                }
                else
                {
                    commandIssuer.enqueueCommand(new StartMessage5th(this));
                }
                break;
            case SEQ_START_5TH:
                commandIssuer.enqueueCommand(new StatusRequestMessage(this));
                break;

            case SEQ_STATUS_REQUEST:
                commandIssuer.enqueueCommand(new QueryCameraCapabilities(this));
                break;

            case SEQ_QUERY_CAMERA_CAPABILITIES:
                commandIssuer.enqueueCommand(new CameraRemoteMessage(this));
                break;

            case SEQ_CAMERA_REMOTE:
                connectFinished();
                break;

            default:
                Log.v(TAG, "RECEIVED UNKNOWN ID : " + id);
                onConnectError(context.getString(R.string.connect_receive_unknown_message));
                break;
        }
    }

    private void sendRegistrationMessage()
    {
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start));
        commandIssuer.enqueueCommand(new RegistrationMessage(this));
    }

    private boolean checkRegistrationMessage(byte[] receiveData)
    {
        // 応答エラーかどうかをチェックする
        if (receiveData.length == 8)
        {
            if ((receiveData[0] == 0x05) && (receiveData[1] == 0x00) && (receiveData[2] == 0x00) && (receiveData[3] == 0x00) &&
                    (receiveData[4] == 0x19) && (receiveData[5] == 0x20) && (receiveData[6] == 0x00) && (receiveData[7] == 0x00)) {
                // 応答エラー...
                return (false);
            }
            return (false);
        }
        return (true);
    }


    private void connectFinished()
    {
        try
        {
            // ちょっと待つ
            Thread.sleep(1000);
            interfaceProvider.getAsyncEventCommunication().connect();
            interfaceProvider.getCameraStatusWatcher().startStatusWatch(interfaceProvider.getStatusListener());
            onConnectNotify();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void onConnectNotify()
    {
        try
        {
            final Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    // カメラとの接続確立を通知する
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected));
                    cameraStatusReceiver.onCameraConnected();
                    Log.v(TAG, "onConnectNotify()");
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
