package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
    private static final int BUFFER_SIZE = 1024 * 1024 + 16;  // 受信バッファは 256kB
    private static final int COMMAND_SEND_RECEIVE_DURATION_MS = 5;
    private static final int COMMAND_SEND_RECEIVE_DURATION_MAX = 1000;
    private static final int COMMAND_POLL_QUEUE_MS = 5;

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

    @Override
    public boolean flushHoldQueue()
    {
        Log.v(TAG, "  flushHoldQueue()");
        holdCommandQueue.clear();
        System.gc();
        return (true);
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
    private boolean receive_from_camera(@NonNull IPtpIpCommand command)
    {
        IPtpIpCommandCallback callback = command.responseCallback();
        if ((callback != null)&&(callback.isReceiveMulti()))
        {
            // 受信したら逐次「受信したよ」と応答するパターン
            return (receive_multi(command));
        }
        //  受信した後、すべてをまとめて「受信したよ」と応答するパターン
        return (receive_single(command));
    }

    private boolean receive_single(@NonNull IPtpIpCommand command)
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
            int receive_message_buffer_size = BUFFER_SIZE;
            byte[] byte_array = new byte[receive_message_buffer_size];
            InputStream is = socket.getInputStream();
            if (is == null)
            {
                Log.v(TAG, " InputStream is NULL... RECEIVE ABORTED.");
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

            // 受信したデータをバッファに突っ込む
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            while (read_bytes > 0)
            {
                read_bytes = is.read(byte_array, 0, receive_message_buffer_size);
                if (read_bytes <= 0)
                {
                    Log.v(TAG, " RECEIVED MESSAGE FINISHED (" + read_bytes + ")");
                    break;
                }
                byteStream.write(byte_array, 0, read_bytes);
                sleep(delayMs);
                read_bytes = is.available();
            }
            ByteArrayOutputStream outputStream = cutHeader(byteStream);
            receivedAllMessage(isDumpReceiveLog, id, outputStream.toByteArray(), callback);
            System.gc();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.gc();
        }
        return (false);
    }

    private void receivedAllMessage(boolean isDumpReceiveLog, int id, byte[] body, IPtpIpCommandCallback callback)
    {
        if (isDumpReceiveLog)
        {
            // ログに受信メッセージを出力する
            Log.v(TAG, "receivedAllMessage() : " + body.length + " bytes.");
            dump_bytes("RECV[" + body.length + "] ", body);
        }
        if (callback != null)
        {
            callback.receivedMessage(id, body);
        }
    }

    private boolean receive_multi(@NonNull IPtpIpCommand command)
    {
        int id = command.getId();
        IPtpIpCommandCallback callback = command.responseCallback();
        int delayMs = command.receiveDelayMs();
        if ((delayMs < 0)||(delayMs > COMMAND_SEND_RECEIVE_DURATION_MAX))
        {
            delayMs = COMMAND_SEND_RECEIVE_DURATION_MS;
        }

        try
        {
            Log.v(TAG, " ===== receive_multi() =====");
            int receive_message_buffer_size = BUFFER_SIZE;
            byte[] byte_array = new byte[receive_message_buffer_size];
            InputStream is = socket.getInputStream();
            if (is == null)
            {
                Log.v(TAG, " InputStream is NULL... RECEIVE ABORTED.");
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

            // 初回データの読み込み
            read_bytes = is.read(byte_array, 0, receive_message_buffer_size);
            int target_length = parseDataLength(byte_array, read_bytes);
            int received_length = read_bytes;

            //  一時的な処理
            if (callback != null)
            {
                //Log.v(TAG, "  --- CALL : read_bytes : "+ received_length + " : total_length : " + target_length + "  buffer SIZE : " + byte_array.length);
                callback.onReceiveProgress(received_length, target_length, Arrays.copyOfRange(byte_array, 0, read_bytes));
            }

            sleep(delayMs);
            read_bytes = is.available();
            while (read_bytes > 0)
            {
                read_bytes = is.read(byte_array, 0, receive_message_buffer_size);
                if (read_bytes <= 0)
                {
                    Log.v(TAG, " RECEIVED MESSAGE FINISHED (" + read_bytes + ")");
                    break;
                }
                received_length = received_length + read_bytes;

                //  一時的な処理
                if (callback != null)
                {
                    //Log.v(TAG, "  --- CALL : read_bytes : "+ read_bytes + " total_read : " + received_length + " : total_length : " + target_length + "  buffer SIZE : " + byte_array.length);
                    callback.onReceiveProgress(received_length, target_length, Arrays.copyOfRange(byte_array, 0, read_bytes));
                }

                //byteStream.write(byte_array, 0, read_bytes);
                sleep(delayMs);
                read_bytes = is.available();
            }
            //ByteArrayOutputStream outputStream = cutHeader(byteStream);
            //receivedMessage(isDumpReceiveLog, id, outputStream.toByteArray(), callback);

            //  終了報告...一時的？
            if (callback != null)
            {
                callback.receivedMessage(id, null);
            }
            System.gc();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.gc();
        }
        return (false);
    }

    private int parseDataLength(byte[] byte_array, int read_bytes)
    {
        int lenlen = 0;
        int packetType = 0;
        try
        {
            if ((read_bytes > 20)&&((int) byte_array[4] == 0x09))
            {
                lenlen = ((((int) byte_array[15]) & 0xff) << 24) + ((((int) byte_array[14]) & 0xff) << 16) + ((((int) byte_array[13]) & 0xff) << 8) + (((int) byte_array[12]) & 0xff);
                packetType = (((int)byte_array[16]) & 0xff);
            }
            Log.v(TAG, " --- parseDataLength() length: " + lenlen + " TYPE: " + packetType + " read_bytes: " + read_bytes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (lenlen);
    }

    private ByteArrayOutputStream cutHeader(ByteArrayOutputStream receivedBuffer)
    {
        try
        {
            byte[] byte_array = receivedBuffer.toByteArray();
            int limit = byte_array.length;
            int lenlen = 0;
            int len = ((((int) byte_array[3]) & 0xff) << 24) + ((((int) byte_array[2]) & 0xff) << 16) + ((((int) byte_array[1]) & 0xff) << 8) + (((int) byte_array[0]) & 0xff);
            int packetType = (((int) byte_array[4]) & 0xff);
            if ((limit == len)||(limit < 16384))
            {
                // 応答は１つしか入っていない。もしくは受信データサイズが16kBの場合は、そのまま返す。
                return (receivedBuffer);
            }
            if (packetType == 0x09)
            {
                lenlen = ((((int) byte_array[15]) & 0xff) << 24) + ((((int) byte_array[14]) & 0xff) << 16) + ((((int) byte_array[13]) & 0xff) << 8) + (((int) byte_array[12]) & 0xff);
                packetType = (((int) byte_array[16]) & 0xff);
            }
            Log.v(TAG, " ---  RECEIVED MESSAGE : " + len + " bytes (BUFFER: " + byte_array.length + " bytes)" + " length : " + lenlen + " TYPE : " + packetType + " --- ");
            if (lenlen == 0)
            {
                // データとしては変なので、なにもしない
                return (receivedBuffer);
            }
            ByteArrayOutputStream outputStream =  new ByteArrayOutputStream();
            //outputStream.write(byte_array, 0, 20);  //
            int position = 20;  // ヘッダ込の先頭
            while (position < limit)
            {
                lenlen = ((((int) byte_array[position + 3]) & 0xff) << 24) + ((((int) byte_array[position + 2]) & 0xff) << 16) + ((((int) byte_array[position + 1]) & 0xff) << 8) + (((int) byte_array[position]) & 0xff);
                packetType = (((int) byte_array[position + 4]) & 0xff);
                if (packetType != 0x0a)
                {
                    Log.v(TAG, " <><><> PACKET TYPE : " + packetType + " LENGTH : " + lenlen);
                }
                int copyByte = ((lenlen - 12) > (limit - (position + 12))) ? (limit - (position + 12)) : (lenlen - 12);
                outputStream.write(byte_array, (position + 12), copyByte);
                position = position + lenlen;
            }
            return (outputStream);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.gc();
        }
        return (receivedBuffer);
    }

    private int waitForReceive(InputStream is, int delayMs)
    {
        int retry_count = 50;
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
}
