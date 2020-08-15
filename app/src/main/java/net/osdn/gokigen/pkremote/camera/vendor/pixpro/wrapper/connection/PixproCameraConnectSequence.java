package net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.connection;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.IPixproInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.IConnectionKeyReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.IPixproCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.IPixproMessages;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence01;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence02;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence03;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence04;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence05;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence06;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence07;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence08;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence09;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence10;
import net.osdn.gokigen.pkremote.camera.vendor.pixpro.wrapper.command.messages.connection.PixproConnectSequence11;

import java.util.Arrays;

public class PixproCameraConnectSequence implements Runnable, IPixproCommandCallback, IPixproMessages
{
    private final String TAG = this.toString();

    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final IPixproInterfaceProvider interfaceProvider;
    private final IPixproCommandPublisher commandIssuer;
    //private final PixproStatusChecker statusChecker;

    PixproCameraConnectSequence(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection, @NonNull IPixproInterfaceProvider interfaceProvider)  // , @NonNull PixproStatusChecker statusChecker)
    {
        Log.v(TAG, " KodakCameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
        this.interfaceProvider = interfaceProvider;
        this.commandIssuer = interfaceProvider.getCommandPublisher();
        //this.statusChecker = statusChecker;
    }

    @Override
    public void run()
    {
        try
        {
            // カメラとTCP接続
            IPixproCommandPublisher issuer = interfaceProvider.getCommandPublisher();
            if (!issuer.isConnected())
            {
                if (!interfaceProvider.getCommandCommunication().connect())
                {
                    // 接続失敗...
                    interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed_pixpro), false, true, Color.RED);
                    onConnectError(context.getString(R.string.dialog_title_connect_failed_pixpro));
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
            startConnectSequence();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed_pixpro), false, true, Color.RED);
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
        switch (id)
        {
            case SEQ_CONNECT_01:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.pixpro_connect_connecting1), false, false, 0);
                commandIssuer.enqueueCommand(new PixproConnectSequence02(this));
                break;

            case SEQ_CONNECT_02:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.pixpro_connect_connecting2), false, false, 0);
                commandIssuer.enqueueCommand(new PixproConnectSequence03(this));
                break;

            case SEQ_CONNECT_03:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.pixpro_connect_connecting3), false, false, 0);
                commandIssuer.enqueueCommand(new PixproConnectSequence04(this));
                break;
            case SEQ_CONNECT_04:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.pixpro_connect_connecting4), false, false, 0);
                // ここで、キー文字列の Base64情報を切り出す(FC 03 の応答、 0x0058 ～ 64バイトの文字列を切り出して、Base64エンコードする)
                getKeyString(rx_body);
                commandIssuer.enqueueCommand(new PixproConnectSequence05(this));
                break;
            case SEQ_CONNECT_05:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.pixpro_connect_connecting5), false, false, 0);
                // ここで、パスワードの情報を切り出す (FE 03 の応答、 0x0078 ～ 文字列を切り出す。)
                getPassword(rx_body);
                commandIssuer.enqueueCommand(new PixproConnectSequence06(this));
                break;
            case SEQ_CONNECT_06:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.pixpro_connect_connecting6), false, false, 0);
                commandIssuer.enqueueCommand(new PixproConnectSequence07(this));
                break;
            case SEQ_CONNECT_07:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.pixpro_connect_connecting7), false, false, 0);
                commandIssuer.enqueueCommand(new PixproConnectSequence08(this));
                break;
            case SEQ_CONNECT_08:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.pixpro_connect_connecting8), false, false, 0);
                commandIssuer.enqueueCommand(new PixproConnectSequence09(this));
                break;
            case SEQ_CONNECT_09:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.pixpro_connect_connecting9), false, false, 0);
                commandIssuer.enqueueCommand(new PixproConnectSequence10(this));
                break;
            case SEQ_CONNECT_10:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.pixpro_connect_connecting10), false, false, 0);
                commandIssuer.enqueueCommand(new PixproConnectSequence11(this));
                break;
            case SEQ_CONNECT_11:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connect_finished), false, false, 0);
                connectFinished();
                Log.v(TAG, "  CONNECT TO CAMERA : DONE.");
                break;
            default:
                Log.v(TAG, " RECEIVED UNKNOWN ID : " + id);
                onConnectError(context.getString(R.string.connect_receive_unknown_message));
                break;
        }
    }

    private void startConnectSequence()
    {
        interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_start), false, false, 0);
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start));
        commandIssuer.enqueueCommand(new PixproConnectSequence01(this));
    }


    private void getPassword(byte[] rx_body)
    {
        try
        {
            int startPosition = 0x78;
            int index = 0x00;
            while (((startPosition + index) < rx_body.length)&&(rx_body[startPosition + index] != (byte) 0x00))
            {
                index++;
            }
            if ((startPosition + index) <= rx_body.length)
            {
                String password = new String(rx_body, startPosition, index);
                IConnectionKeyReceiver receiver = interfaceProvider.getConnectionKeyReceiver();
                if (receiver != null)
                {
                    receiver.receivedPassword(password);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getKeyString(byte[] rx_body)
    {
        try
        {
            int startPosition = 0x58;
            int index = 0x00;
            while (((startPosition + index) < rx_body.length)&&(rx_body[startPosition + index] != (byte) 0x00))
            {
                index++;
            }
            if ((startPosition + index) <= rx_body.length)
            {
                byte[] keyString = Arrays.copyOfRange(rx_body, startPosition, (startPosition + index));
                IConnectionKeyReceiver receiver = interfaceProvider.getConnectionKeyReceiver();
                if (receiver != null)
                {
                    receiver.receivedKeyString(keyString);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void connectFinished()
    {
        try
        {
            // 接続成功のメッセージを出す
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connected), false, false, 0);

            // ちょっと待つ
            Thread.sleep(1000);

            //interfaceProvider.getAsyncEventCommunication().connect();
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
                    Log.v(TAG, " onConnectNotify()");
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
