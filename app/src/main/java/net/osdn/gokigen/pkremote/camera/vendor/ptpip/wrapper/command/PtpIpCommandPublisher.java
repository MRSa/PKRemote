package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import static net.osdn.gokigen.pkremote.camera.utils.SimpleLogDumper.dump_bytes;

public class PtpIpCommandPublisher implements IPtpIpCommandPublisher, IPtpIpCommunication
{
    private static final String TAG = PtpIpCommandPublisher.class.getSimpleName();

    private static final int SEQUENCE_START_NUMBER = 1;
    private static final int BUFFER_SIZE = 1024 * 1024 * 24 + 8;
    private static final int COMMAND_SEND_RECEIVE_DURATION_MS = 50;
    private static final int COMMAND_SEND_RECEIVE_DURATION_MAX = 1000;
    private static final int COMMAND_POLL_QUEUE_MS = 150;

    private final String ipAddress;
    private final int portNumber;

    private boolean isStart = false;
    private boolean isHold = false;
    private int holdId = 0;
    private Socket socket = null;
    private DataOutputStream dos = null;
    private BufferedReader bufferedReader = null;
    private int sequenceNumber = SEQUENCE_START_NUMBER;
    private Queue<IPtpIpCommand> commandQueue;
    private Queue<IPtpIpCommand> holdCommandQueue;

    public PtpIpCommandPublisher(@NonNull String ip, int portNumber)
    {
        this.ipAddress = ip;
        this.portNumber = portNumber;
        this.commandQueue = new ArrayDeque<>();
        this.holdCommandQueue = new ArrayDeque<>();
        commandQueue.clear();
        holdCommandQueue.clear();
    }

    @Override
    public boolean isConnected()
    {
        return (socket != null);
    }

    @Override
    public boolean connect()
    {
        try
        {
            socket = new Socket(ipAddress, portNumber);
            return (true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            socket = null;
        }
        return (false);
    }

    @Override
    public void disconnect()
    {
        // ストリームを全部閉じる
        try
        {
            if (dos != null)
            {
                dos.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        dos = null;

        try
        {
            if (bufferedReader != null)
            {
                bufferedReader.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        bufferedReader = null;

        try
        {
            if (socket != null)
            {
                socket.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        socket = null;
        sequenceNumber = SEQUENCE_START_NUMBER;
        System.gc();
    }

    @Override
    public void start()
    {
        if (isStart)
        {
            // すでにコマンドのスレッド動作中なので抜ける
            return;
        }
        isStart = true;

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    dos = new DataOutputStream(socket.getOutputStream());
                    while (isStart)
                    {
                        try
                        {
                            IPtpIpCommand command = commandQueue.poll();
                            if (command != null)
                            {
                                issueCommand(command);
                            }
                            Thread.sleep(COMMAND_POLL_QUEUE_MS);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.v(TAG, "<<<<< IP : " + ipAddress + " port : " + portNumber + " >>>>>");
                    e.printStackTrace();
                }
            }
        });
        try
        {
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stop()
    {
        isStart = false;
        commandQueue.clear();
    }

    @Override
    public boolean enqueueCommand(@NonNull IPtpIpCommand command)
    {
        try
        {
            if (isHold) {
                if (holdId == command.getHoldId()) {
                    if (command.isRelease()) {
                        // コマンドをキューに積んだ後、リリースする
                        boolean ret = commandQueue.offer(command);
                        isHold = false;

                        //  溜まっているキューを積みなおす
                        while (holdCommandQueue.size() != 0) {
                            IPtpIpCommand queuedCommand = holdCommandQueue.poll();
                            commandQueue.offer(queuedCommand);
                            if ((queuedCommand != null)&&(queuedCommand.isHold()))
                            {
                                // 特定シーケンスに入った場合は、そこで積みなおすのをやめる
                                isHold = true;
                                holdId = queuedCommand.getHoldId();
                                break;
                            }
                        }
                        return (ret);
                    }
                    return (commandQueue.offer(command));
                } else {
                    // 特定シーケンスではなかったので HOLD
                    return (holdCommandQueue.offer(command));
                }
            }
            if (command.isHold())
            {
                isHold = true;
                holdId = command.getHoldId();
            }

            //Log.v(TAG, "Enqueue : "  + command.getId());
            return (commandQueue.offer(command));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    private void issueCommand(@NonNull IPtpIpCommand command)
    {
        try
        {
            boolean retry_over = true;
            while (retry_over)
            {
                //Log.v(TAG, "issueCommand : " + command.getId());
                byte[] commandBody = command.commandBody();
                if (commandBody != null)
                {
                    // コマンドボディが入っていた場合には、コマンド送信（入っていない場合は受信待ち）
                    send_to_camera(command.dumpLog(), commandBody, command.useSequenceNumber(), command.embeddedSequenceNumberIndex());
                    byte[] commandBody2 = command.commandBody2();
                    if (commandBody2 != null)
                    {
                        // コマンドボディの２つめが入っていた場合には、コマンドを連続送信する
                        send_to_camera(command.dumpLog(), commandBody2, command.useSequenceNumber(), command.embeddedSequenceNumberIndex2());
                    }
                    byte[] commandBody3 = command.commandBody3();
                    if (commandBody3 != null)
                    {
                        // コマンドボディの３つめが入っていた場合には、コマンドを連続送信する
                        send_to_camera(command.dumpLog(), commandBody3, command.useSequenceNumber(), command.embeddedSequenceNumberIndex3());
                    }
                    if (command.isIncrementSeqNumber())
                    {
                        // シーケンス番号を更新する
                        sequenceNumber++;
                    }
                }
                retry_over = receive_from_camera(command);
                if ((retry_over)&&(commandBody != null))
                {
                    // 再送信...のために、シーケンス番号を戻す...
                    sequenceNumber--;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *    カメラにコマンドを送信する（メイン部分）
     *
     */
    private void send_to_camera(boolean isDumpReceiveLog, byte[] byte_array, boolean useSequenceNumber, int embeddedSequenceIndex)
    {
        try
        {
            //dos = new DataOutputStream(socket.getOutputStream());  // ここにいたらいけない？

            // メッセージボディを加工： 最初に４バイトのレングス長をつける
            byte[] sendData = new byte[byte_array.length + 4];

            sendData[0] = (byte) (byte_array.length + 4);
            sendData[1] = 0x00;
            sendData[2] = 0x00;
            sendData[3] = 0x00;
            System.arraycopy(byte_array,0,sendData,4, byte_array.length);

            if (useSequenceNumber)
            {
                // Sequence Number を反映させる
                sendData[embeddedSequenceIndex] = (byte) ((0x000000ff & sequenceNumber));
                sendData[embeddedSequenceIndex + 1] = (byte) (((0x0000ff00 & sequenceNumber) >>> 8) & 0x000000ff);
                sendData[embeddedSequenceIndex + 2] = (byte) (((0x00ff0000 & sequenceNumber) >>> 16) & 0x000000ff);
                sendData[embeddedSequenceIndex + 3] = (byte) (((0xff000000 & sequenceNumber) >>> 24) & 0x000000ff);
                if (isDumpReceiveLog)
                {
                    Log.v(TAG, "----- SEQ No. : " + sequenceNumber + " -----");
                }
            }

            if (isDumpReceiveLog)
            {
                // ログに送信メッセージを出力する
                dump_bytes("SEND[" + sendData.length + "] ", sendData);
            }

            // (データを)送信
            dos.write(sendData);
            dos.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void sleep(int delayMs)
    {
        try
        {
            Thread.sleep(delayMs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *    カメラからにコマンドの結果を受信する（メイン部分）
     *
     */
    private void receive_from_camera_old(boolean isDumpReceiveLog, int id, IPtpIpCommandCallback callback, boolean receiveAgain, int delayMs)
    {
        try
        {
            sleep(delayMs);
            boolean isFirstTime = true;
            int totalReadBytes;
            int receive_message_buffer_size = BUFFER_SIZE;
            byte[] byte_array = new byte[receive_message_buffer_size];
            InputStream is = socket.getInputStream();
            if (is != null)
            {
                byte[] receive_body = null;
                int read_bytes = 0;
                while (read_bytes == 0)
                {
                    read_bytes = is.available();
                    sleep(delayMs);
                    Log.v(TAG, " is.available() WAIT... ");
                }
                if (read_bytes > 0)
                {
                    read_bytes = is.read(byte_array, 0, receive_message_buffer_size);
                    if (read_bytes > 4)
                    {
                        if (receiveAgain) {
                            int length = ((((int) byte_array[3]) & 0xff) << 24) + ((((int) byte_array[2]) & 0xff) << 16) + ((((int) byte_array[1]) & 0xff) << 8) + (((int) byte_array[0]) & 0xff);
                            if (length > receive_message_buffer_size) {
                                Log.v(TAG, "+++++ TOTAL RECEIVE MESSAGE SIZE IS " + length + " +++++");
                            }
                            totalReadBytes = read_bytes;
                            while ((length > totalReadBytes) || ((length == read_bytes) && ((int) byte_array[4] == 0x02))) {
                                // データについて、もう一回受信が必要な場合...
                                if (isDumpReceiveLog) {
                                    Log.v(TAG, "--- RECEIVE AGAIN --- [" + length + "(" + read_bytes + ") " + byte_array[4] + "] ");
                                }
                                sleep(delayMs);
                                int availableReadBytes = is.available();
                                if (availableReadBytes <= 0) {
                                    // 読めるデータ数がない...よみだし終了にする。
                                    Log.v(TAG, "  is.availableReadBytes() :  " + availableReadBytes);
                                    break;
                                }
                                int read_bytes2 = is.read(byte_array, read_bytes, receive_message_buffer_size - read_bytes);
                                if (read_bytes2 > 0) {
                                    read_bytes = read_bytes + read_bytes2;
                                    totalReadBytes = totalReadBytes + read_bytes2;
                                } else {
                                    // よみだし終了。
                                    Log.v(TAG, "FINISHED RECEIVE... ");
                                    break;
                                }
                                if (callback != null) {
                                    if (callback.isReceiveMulti()) {
                                        int offset = 0;
                                        if (isFirstTime) {
                                            // 先頭のヘッダ部分をカットして送る
                                            offset = 12;
                                            isFirstTime = false;
                                            //Log.v(TAG, " FIRST TIME : " + read_bytes + " " + offset);
                                        }
                                        callback.onReceiveProgress(read_bytes - offset, length, Arrays.copyOfRange(byte_array, offset, read_bytes));
                                        read_bytes = 0;
                                    } else {
                                        callback.onReceiveProgress(read_bytes, length, null);
                                    }
                                }
                            }
                        }
                        receive_body = Arrays.copyOfRange(byte_array, 0, read_bytes);
                    } else {
                        receive_body = new byte[1];
                    }
                }
                if (isDumpReceiveLog)
                {
                    // ログに受信メッセージを出力する
                    Log.v(TAG, "receive_from_camera() : " + read_bytes + " bytes.");
                    if (receive_body != null)
                    {
                        dump_bytes("RECV[" + receive_body.length + "] ", receive_body);
                    }
                }
                if (callback != null)
                {
                    if (callback.isReceiveMulti())
                    {
                        callback.receivedMessage(id, null);
                    }
                    else
                    {
                        callback.receivedMessage(id, receive_body);
                        //callback.receivedMessage(id, Arrays.copyOfRange(receive_body, 0, receive_body.length));
                    }
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    /**
     *    カメラからにコマンドの結果を受信する（メイン部分）
     *
     */
    private boolean receive_from_camera(@NonNull IPtpIpCommand command)
    {
        boolean isDumpReceiveLog = command.dumpLog();
        int id = command.getId();
        IPtpIpCommandCallback callback = command.responseCallback();
        int delayMs = command.receiveDelayMs();
        if ((delayMs < 0)||(delayMs > COMMAND_SEND_RECEIVE_DURATION_MAX))
        {
            delayMs = COMMAND_SEND_RECEIVE_DURATION_MS;
        }

        try
        {
            boolean isFirstTime = true;
            int receive_message_buffer_size = BUFFER_SIZE;
            byte[] byte_array = new byte[receive_message_buffer_size];
            InputStream is = socket.getInputStream();
            if (is == null)
            {
                Log.v(TAG, " InputStream is NULL... RECEIVE ABORTED");
                return (false);
            }

            // 初回データが受信バッファにデータが溜まるまで待つ...
            int read_bytes = waitForReceive(is, delayMs);
            if (read_bytes < 0)
            {
                // リトライオーバー...
                Log.v(TAG, " RECEIVE : RETRY OVER...");
                return (true);
            }

            int position = 0;
            int message_length = receive_message_buffer_size;
            while (read_bytes > 0)
            {
                read_bytes = is.read(byte_array, position, receive_message_buffer_size - position);
                if ((read_bytes <= 0)||(receive_message_buffer_size <= read_bytes + position))
                {
                    Log.v(TAG, " RECEIVED MESSAGE FINISHED (" + position + ")");
                    break;
                }
                if (position == 0)
                {
                    int lenlen = 0;
                    int len = ((((int) byte_array[3]) & 0xff) << 24) + ((((int) byte_array[2]) & 0xff) << 16) + ((((int) byte_array[1]) & 0xff) << 8) + (((int) byte_array[0]) & 0xff);
                    message_length = len;
                    if ((read_bytes > 20)&&((int) byte_array[4] == 0x09))
                    {
                        lenlen = ((((int) byte_array[15]) & 0xff) << 24) + ((((int) byte_array[14]) & 0xff) << 16) + ((((int) byte_array[13]) & 0xff) << 8) + (((int) byte_array[12]) & 0xff);
                        message_length = lenlen;
                    }
                    Log.v(TAG, " RECEIVED MESSAGE LENGTH (" + len + ") [" + lenlen + "]. : " + read_bytes);
                }
                position = position + read_bytes;
                //Log.v(TAG, " RECEIVED POSITION ((" + position + "))");

                if (callback != null)
                {
                    if (callback.isReceiveMulti())
                    {
                        int offset = 0;
                        if (isFirstTime)
                        {
                            // 先頭のヘッダ部分をカットして送る
                            offset = 12;
                            isFirstTime = false;
                            message_length = ((((int) byte_array[3]) & 0xff) << 24) + ((((int) byte_array[2]) & 0xff) << 16) + ((((int) byte_array[1]) & 0xff) << 8) + (((int) byte_array[0]) & 0xff);
                            //Log.v(TAG, " FIRST TIME : " + read_bytes + " " + offset);
                        }
                        callback.onReceiveProgress(read_bytes - offset, message_length, Arrays.copyOfRange(byte_array, offset, read_bytes));
                    }
                    else
                    {
                        callback.onReceiveProgress(read_bytes, message_length, null);
                    }
                }

                sleep(delayMs);
                read_bytes = is.available();
                if (read_bytes <= 0)
                {
                    //Log.v(TAG, " RECEIVED MESSAGE FINISHED : " + position + " bytes.");
                }
            }
            byte[] receive_body = Arrays.copyOfRange(byte_array, 0, (position < 1) ? 1 : position);
            Log.v(TAG, " RECEIVED : [" + position + "]");
            receivedMessage(isDumpReceiveLog, id, receive_body, callback);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    private int waitForReceive(InputStream is, int delayMs)
    {
        int retry_count = 30;
        int read_bytes = 0;
        try
        {
            while (read_bytes <= 0)
            {
                sleep(delayMs);
                read_bytes = is.available();
                if (read_bytes == 0)
                {
                    Log.v(TAG, " is.available() WAIT... ");
                    retry_count--;
                    if (retry_count < 0)
                    {
                        return (-1);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (read_bytes);
    }

    private void receivedMessage(boolean isDumpReceiveLog, int id, byte[] body, IPtpIpCommandCallback callback)
    {
        if (isDumpReceiveLog)
        {
            // ログに受信メッセージを出力する
            Log.v(TAG, "receive_from_camera() : " + body.length + " bytes.");
            dump_bytes("RECV[" + body.length + "] ", body);
        }
        if (callback != null)
        {
            if (callback.isReceiveMulti())
            {
                callback.receivedMessage(id, null);
            }
            else
            {
                callback.receivedMessage(id, body);
                //callback.receivedMessage(id, Arrays.copyOfRange(receive_body, 0, receive_body.length));
            }
        }
    }
}

