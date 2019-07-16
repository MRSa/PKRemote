package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.connection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.changemode.ChangeToPlayback1st;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.changemode.ChangeToPlayback2nd;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.changemode.ChangeToPlayback3rd;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.changemode.ChangeToPlayback4th;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.CameraRemoteMessage;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.RegistrationMessage;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.StartMessage;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.StartMessage2ndRead;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.StartMessage3rd;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.StartMessage4th;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.start.StartMessage5th;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.status.IFujiXRunModeHolder;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;


public class FujiXCameraConnectSequenceForPlayback implements Runnable, IFujiXCommandCallback, IFujiXMessages
{
    private final String TAG = this.toString();

    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final IFujiXInterfaceProvider interfaceProvider;
    private final IFujiXCommandPublisher commandIssuer;
    private boolean isBothLiveView = false;

    FujiXCameraConnectSequenceForPlayback(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection, @NonNull IFujiXInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, " FujiXCameraConnectSequenceForPlayback");
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
                    interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed), false, true, Color.RED);
                    onConnectError(context.getString(R.string.dialog_title_connect_failed));
                    return;
                }
            }
            else
            {
                Log.v(TAG, "SOCKET IS ALREADY CONNECTED...");
            }
            // コマンドタスクの実行開始
            issuer.start();

            // 接続シーケンスの開始
            sendRegistrationMessage();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed), false, true, Color.RED);
            onConnectError(e.getLocalizedMessage());
        }
    }

    private void onConnectError(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        //Log.v(TAG, "receivedMessage : " + id + "[" + rx_body.length + " bytes]");
        //int bodyLength = 0;
        IFujiXRunModeHolder runModeHolder;
        switch (id)
        {
            case SEQ_REGISTRATION:
                if (checkRegistrationMessage(rx_body))
                {
                    commandIssuer.enqueueCommand(new StartMessage(this));
                }
                else
                {
                    onConnectError(context.getString(R.string.connect_error_message));
                }
                break;

            case SEQ_START:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting1), false, false, 0);
                commandIssuer.enqueueCommand(new StartMessage2ndRead(this));
                break;

            case SEQ_START_2ND_READ:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting2), false, false, 0);
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
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting3), false, false, 0);
                commandIssuer.enqueueCommand(new StartMessage3rd(this));
                break;

            case SEQ_START_3RD:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting4), false, false, 0);
                commandIssuer.enqueueCommand(new StartMessage4th(this));
                break;

            case SEQ_START_4TH:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting5), false, false, 0);
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
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting6), false, false, 0);
                commandIssuer.enqueueCommand(new QueryCameraCapabilities(this));
                //commandIssuer.enqueueCommand(new StatusRequestMessage(this));
                break;

            case SEQ_QUERY_CAMERA_CAPABILITIES:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting7), false, false, 0);
                commandIssuer.enqueueCommand(new CameraRemoteMessage(this));
                break;

            case SEQ_CAMERA_REMOTE:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting8), false, false, 0);
                commandIssuer.enqueueCommand(new ChangeToPlayback1st(this));
                runModeHolder = interfaceProvider.getRunModeHolder();
                if (runModeHolder != null)
                {
                    runModeHolder.transitToPlaybackMode(false);
                }
                //connectFinished();
                break;

            case SEQ_CHANGE_TO_PLAYBACK_1ST:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting9), false, false, 0);
                commandIssuer.enqueueCommand(new ChangeToPlayback2nd(this));
                break;

            case SEQ_CHANGE_TO_PLAYBACK_2ND:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting10), false, false, 0);
                commandIssuer.enqueueCommand(new ChangeToPlayback3rd(this));
                break;

            case SEQ_CHANGE_TO_PLAYBACK_3RD:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting11), false, false, 0);
                commandIssuer.enqueueCommand(new ChangeToPlayback4th(this));
                break;

            case SEQ_CHANGE_TO_PLAYBACK_4TH:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connecting12), false, false, 0);
                commandIssuer.enqueueCommand(new StatusRequestMessage(this));
                break;

            case SEQ_STATUS_REQUEST:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connect_finished), false, false, 0);
                IFujiXCommandCallback callback = interfaceProvider.getStatusHolder();
                if (callback != null)
                {
                    callback.receivedMessage(id, rx_body);
                }
                runModeHolder = interfaceProvider.getRunModeHolder();
                if (runModeHolder != null)
                {
                    runModeHolder.transitToPlaybackMode(true);
                }
                connectFinished();
                Log.v(TAG, "CHANGED PLAYBACK MODE : DONE.");
                break;

            default:
                Log.v(TAG, "RECEIVED UNKNOWN ID : " + id);
                onConnectError(context.getString(R.string.connect_receive_unknown_message));
                break;
        }
    }

    private void sendRegistrationMessage()
    {
        interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_start), false, false, 0);
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start));
        commandIssuer.enqueueCommand(new RegistrationMessage(this));
    }

    private boolean checkRegistrationMessage(byte[] receiveData)
    {
        // データがないときにはエラー
        if ((receiveData == null)||(receiveData.length < 8))
        {
            return (false);
        }

        // 応答エラーかどうかをチェックする
        if (receiveData.length == 8)
        {
            if ((receiveData[0] == 0x05) && (receiveData[1] == 0x00) && (receiveData[2] == 0x00) && (receiveData[3] == 0x00) &&
                    (receiveData[4] == 0x19) && (receiveData[5] == 0x20) && (receiveData[6] == 0x00) && (receiveData[7] == 0x00)) {
                // 応答エラー...
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.error_reply_from_camera), false, true, Color.RED);
                return (false);
            }
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.other_error_reply_from_camera), false, true, Color.RED);
            return (false);
        }
        interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.registration_reply_from_camera), false, false, 0);
        return (true);
    }


    private void connectFinished()
    {
        try
        {
            // 接続成功のメッセージを出す
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connected), false, false, 0);

            // ちょっと待つ
            Thread.sleep(1000);

            interfaceProvider.getAsyncEventCommunication().connect();
            //interfaceProvider.getCameraStatusWatcher().startStatusWatch(interfaceProvider.getStatusListener());  ステータスの定期確認は実施しない

            // 接続成功！のメッセージを出す
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connected), false, false, 0);

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
