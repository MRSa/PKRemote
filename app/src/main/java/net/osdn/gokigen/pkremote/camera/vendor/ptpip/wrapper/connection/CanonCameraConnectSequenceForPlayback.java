package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.connection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonInitEventRequest;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonRegistrationMessage;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status.IPtpIpRunModeHolder;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;


public class CanonCameraConnectSequenceForPlayback implements Runnable, IPtpIpCommandCallback, IPtpIpMessages
{
    private final String TAG = this.toString();

    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final IPtpIpInterfaceProvider interfaceProvider;
    private final IPtpIpCommandPublisher commandIssuer;
    private boolean isBothLiveView = false;

    CanonCameraConnectSequenceForPlayback(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection, @NonNull IPtpIpInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, " CanonCameraConnectSequenceForPlayback");
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
/*
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
*/
            // カメラとTCP接続
            IPtpIpCommandPublisher issuer = interfaceProvider.getCommandPublisher();
            if (!issuer.isConnected())
            {
                if (!interfaceProvider.getCommandCommunication().connect())
                {
                    // 接続失敗...
                    interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed_canon), false, true, Color.RED);
                    onConnectError(context.getString(R.string.dialog_title_connect_failed_canon));
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
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed_canon), false, true, Color.RED);
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
        IPtpIpRunModeHolder runModeHolder;
        switch (id)
        {
/**/
            case SEQ_REGISTRATION:
                if (checkRegistrationMessage(rx_body))
                {
                    sendInitEventRequest(rx_body);
                }
                else
                {
                    onConnectError(context.getString(R.string.connect_error_message));
                }
                break;

            case SEQ_EVENT_INITIALIZE:
                if (checkEventInitialize(rx_body))
                {
                    interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting1), false, false, 0);
                    commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_OPEN_SESSION, 0x1002));
                }
                else
                {
                    onConnectError(context.getString(R.string.connect_error_message));
                }
                break;

            case SEQ_OPEN_SESSION:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting2), false, false, 0);
                //commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_OPEN_SESSION));
                break;

/*
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
                IPtpIpCommandCallback callback = interfaceProvider.getStatusHolder();
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
*/
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
        commandIssuer.enqueueCommand(new CanonRegistrationMessage(this));
    }

    private void sendInitEventRequest(byte[] receiveData)
    {
        interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_start_2), false, false, 0);
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start_2));
        int connectionNumber = 0;
        try
        {
            connectionNumber = (receiveData[8] & 0xff);
            connectionNumber = connectionNumber + ((receiveData[9]  & 0xff) << 8);
            connectionNumber = connectionNumber + ((receiveData[10] & 0xff) << 16);
            connectionNumber = connectionNumber + ((receiveData[11] & 0xff) << 24);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        commandIssuer.enqueueCommand(new CanonInitEventRequest(this, connectionNumber));
    }

    private boolean checkRegistrationMessage(byte[] receiveData)
    {
        // データ(Connection Number)がないときにはエラーと判断する
        if ((receiveData == null)||(receiveData.length < 12))
        {
            return (false);
        }
        return (true);
    }

    private boolean checkEventInitialize(byte[] receiveData)
    {
        Log.v(TAG, "checkEventInitialize() ");
        if (receiveData == null)
        {
            return (false);
        }
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
