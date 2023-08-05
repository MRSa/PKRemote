package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command

import android.util.Log
import net.osdn.gokigen.pkremote.camera.utils.SimpleLogDumper
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

class PtpIpCommandPublisher(private val tcpNoDelay : Boolean, private val waitForever: Boolean) : IPtpIpCommandPublisher, IPtpIpCommunication
{
    private var isConnected = false
    private var isStart = false
    private var isHold = false
    private var holdId = 0

    private var socket : Socket? = null
    private var dos: DataOutputStream? = null
    private var bufferedReader: BufferedReader? = null

    private var sequenceNumber = SEQUENCE_START_NUMBER
    private var commandQueue: Queue<IPtpIpCommand> = ArrayDeque()
    private var holdCommandQueue: Queue<IPtpIpCommand> = ArrayDeque()

    init
    {
        commandQueue.clear()
        holdCommandQueue.clear()
    }

    override fun isConnected(): Boolean
    {
        return (isConnected)
    }

    override fun connect(ipAddress: String, portNumber: Int): Boolean
    {
        try
        {
            Log.v(TAG, " connect()")
            socket = Socket()
            socket?.tcpNoDelay = tcpNoDelay
            if (tcpNoDelay)
            {
                //socket?.tcpNoDelay = true
                //socket?.keepAlive = false
                socket?.keepAlive = false
                //socket?.setPerformancePreferences(0, 1, 2)
                socket?.setPerformancePreferences(0, 1, 2)
                //socket?.setPerformancePreferences(0, 1, 2)
                //socket?.setPerformancePreferences(1, 0, 0)
                //socket?.setPerformancePreferences(0, 0, 2)
                socket?.oobInline = true
                //socket?.reuseAddress = false
                socket?.trafficClass = 0x80 // 0x80
                socket?.soTimeout = 0
                //socket?.soTimeout = 0
                //socket?.receiveBufferSize = 8192  // 10240 // 16384 // 6144// 8192 // 49152 // 65536 // 32768
                //socket?.sendBufferSize = 1024 // 8192 // 4096 // 2048 // 10240
                socket?.setSoLinger(true, 500)
                //socket?.setReceiveBufferSize(2097152);
                //socket?.setSendBufferSize(524288);

                Log.v(TAG, " SOCKET (SEND:${socket?.sendBufferSize}, RECV:${socket?.receiveBufferSize}) oob:${socket?.oobInline} SO_TIMEOUT:${socket?.soTimeout}ms trafficClass:${socket?.trafficClass}")
            }
            socket?.connect(InetSocketAddress(ipAddress, portNumber), 0)
            Log.v(TAG, "  connect -> IP : $ipAddress port : $portNumber")
            isConnected = true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            isConnected = false
            socket = null
            Log.v(TAG, "<<<<< IP : $ipAddress port : $portNumber >>>>>")
        }
        return (isConnected)
    }

    override fun disconnect()
    {
        try
        {
            dos?.close()
            bufferedReader?.close()
            socket?.close()
            commandQueue.clear()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
        System.gc()

        sequenceNumber = SEQUENCE_START_NUMBER
        isConnected = false
        isStart = false
        dos = null
        bufferedReader = null
        socket = null
    }

    override fun start()
    {
        if (isStart)
        {
            // すでにコマンドのスレッド動作中なので抜ける
            return
        }
        if (socket == null)
        {
            isStart = false
            Log.v(TAG, " SOCKET IS NULL. (cannot start)")
            return
        }

        isStart = true
        Log.v(TAG, " start()")
        val thread = Thread {
            try
            {
                dos = DataOutputStream(socket?.getOutputStream())
                while (isStart)
                {
                    try
                    {
                        val command = commandQueue.poll()
                        command?.let { issueCommand(it) }
                        Thread.sleep(COMMAND_POLL_QUEUE_MS.toLong())
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
        try
        {
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun stop()
    {
        isStart = false
        commandQueue.clear()
    }

    override fun enqueueCommand(command: IPtpIpCommand): Boolean
    {
        try
        {
            if (isHold)
            {
                return (if (holdId == command.holdId) {
                    if (command.isRelease)
                    {
                        // コマンドをキューに積んだ後、リリースする
                        val ret = commandQueue.offer(command)
                        isHold = false

                        //  溜まっているキューを積みなおす
                        while (holdCommandQueue.size != 0)
                        {
                            val queuedCommand = holdCommandQueue.poll()
                            commandQueue.offer(queuedCommand)
                            if (queuedCommand != null && queuedCommand.isHold)
                            {
                                // 特定シーケンスに入った場合は、そこで積みなおすのをやめる
                                isHold = true
                                holdId = queuedCommand.holdId
                                break
                            }
                        }
                        return ret
                    }
                    commandQueue.offer(command)
                }
                else
                {
                    // 特定シーケンスではなかったので HOLD
                    holdCommandQueue.offer(command)
                })
            }
            if (command.isHold)
            {
                isHold = true
                holdId = command.holdId
            }
            if (commandQueue.size > 1)
            {
                // たまっているときだけログを吐く
                Log.v(TAG, "Enqueue [ID: " + command.id + "] size: " + commandQueue.size)
            }
            return (commandQueue.offer(command))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    override fun getCurrentQueueSize(): Int
    {
        return commandQueue.size
    }

    override fun flushQueue(): Boolean
    {
        Log.v(TAG, "  flushQueue()  size: ${commandQueue.size}")
        // TODO: たまっているキューをダンプする
        commandQueue.clear()
        System.gc()
        return (true)
    }

    override fun isExistCommandMessageQueue(id: Int): Int
    {
        var count = 0
        for (cmd in commandQueue)
        {
            if (cmd.id == id)
            {
                count++
            }
        }
       return count
    }

    override fun flushHoldQueue(): Boolean
    {
        Log.v(TAG, "  flushHoldQueue()")
        holdCommandQueue.clear()
        System.gc()
        return (true)
    }

    private fun issueCommand(command: IPtpIpCommand)
    {
        try
        {
            var retryOver = true
            while (retryOver)
            {
                //Log.v(TAG, "issueCommand : " + command.getId());
                val commandBody = command.commandBody()
                if (commandBody != null)
                {
                    // コマンドボディが入っていた場合には、コマンド送信（入っていない場合は受信待ち）
                    sendToCamera(command.dumpLog(), commandBody, command.useSequenceNumber(), command.embeddedSequenceNumberIndex())
                    val commandBody2 = command.commandBody2()
                    if (commandBody2 != null)
                    {
                        // コマンドボディの２つめが入っていた場合には、コマンドを連続送信する
                        sendToCamera(command.dumpLog(), commandBody2, command.useSequenceNumber(), command.embeddedSequenceNumberIndex2())
                    }
                    val commandBody3 = command.commandBody3()
                    if (commandBody3 != null)
                    {
                        // コマンドボディの３つめが入っていた場合には、コマンドを連続送信する
                        sendToCamera(command.dumpLog(), commandBody3, command.useSequenceNumber(), command.embeddedSequenceNumberIndex3())
                    }
                    if (command.isIncrementSeqNumber)
                    {
                        // シーケンス番号を更新する
                        sequenceNumber++
                    }
                }
                retryOver = receiveFromCamera(command)
                if ((retryOver)&&(commandBody != null))
                {
                    if (!command.isRetrySend)
                    {
                        while (retryOver)
                        {
                            //  コマンドを再送信しない場合はここで応答を待つ...
                            retryOver = receiveFromCamera(command)
                        }
                        break
                    }
                    if (!command.isIncrementSequenceNumberToRetry)
                    {
                        // 再送信...のために、シーケンス番号を戻す...
                        sequenceNumber--
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * カメラにコマンドを送信する（メイン部分）
     *
     */
    private fun sendToCamera(isDumpReceiveLog: Boolean, byteArray: ByteArray, useSequenceNumber: Boolean, embeddedSequenceIndex: Int)
    {
        try
        {
            if (dos == null)
            {
                Log.v(TAG, " DataOutputStream is null.")
                return
            }

            // メッセージボディを加工： 最初に４バイトのレングス長をつける
            val sendData = ByteArray(byteArray.size + 4)
            sendData[0] = (byteArray.size + 4).toByte()
            sendData[1] = 0x00
            sendData[2] = 0x00
            sendData[3] = 0x00
            System.arraycopy(byteArray, 0, sendData, 4, byteArray.size)
            if (useSequenceNumber)
            {
                // Sequence Number を反映させる
                sendData[embeddedSequenceIndex    ] = (0x000000ff and sequenceNumber).toByte()
                sendData[embeddedSequenceIndex + 1] = (0x0000ff00 and sequenceNumber ushr 8 and 0x000000ff).toByte()
                sendData[embeddedSequenceIndex + 2] = (0x00ff0000 and sequenceNumber ushr 16 and 0x000000ff).toByte()
                sendData[embeddedSequenceIndex + 3] = (-0x1000000 and sequenceNumber ushr 24 and 0x000000ff).toByte()
                if (isDumpReceiveLog)
                {
                    Log.v(TAG, "----- SEQ No. : $sequenceNumber -----")
                }
            }
            if (isDumpReceiveLog)
            {
                // ログに送信メッセージを出力する
                SimpleLogDumper.dump_bytes("SEND[" + sendData.size + "] ", sendData)
            }

            // (データを)送信
            dos?.write(sendData)
            dos?.flush()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun sleep(delayMs: Int)
    {
        try
        {
            Thread.sleep(delayMs.toLong())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * カメラからにコマンドの結果を受信する（メイン部分）
     *
     */
    private fun receiveFromCamera(command: IPtpIpCommand): Boolean
    {
        val callback = command.responseCallback()
        var delayMs = command.receiveDelayMs()
        if (delayMs < 0 || delayMs > COMMAND_SEND_RECEIVE_DURATION_MAX)
        {
            delayMs = COMMAND_SEND_RECEIVE_DURATION_MS
        }

        return (if (callback != null && callback.isReceiveMulti)
        {
            // 受信したら逐次「受信したよ」と応答するパターン
            //Log.v(TAG, " receiveMulti() : $delayMs [id:${command.id}] SEQ: $sequenceNumber")
            receiveMulti(command, delayMs)
        }
        else
        {
            //Log.v(TAG, " receiveSingle() : $delayMs [id:${command.id}] SEQ: $sequenceNumber")
            receiveSingle(command, delayMs)
        })
        //  受信した後、すべてをまとめて「受信したよ」と応答するパターン
    }

    private fun receiveSingle(command: IPtpIpCommand, delayMs: Int): Boolean
    {
        val isDumpReceiveLog = command.dumpLog()
        val id = command.id
        val callback = command.responseCallback()
        try
        {
            val receiveMessageBufferSize = BUFFER_SIZE
            val byteArray = ByteArray(receiveMessageBufferSize)
            val inputStream = socket?.getInputStream()
            if (inputStream == null)
            {
                Log.v(TAG, " InputStream is NULL... RECEIVE ABORTED.")
                receivedAllMessage(isDumpReceiveLog, id, null, callback)
                return (false)
            }

            // 初回データが受信バッファにデータが溜まるまで待つ...
            var readBytes = waitForReceive(inputStream, delayMs, command.maxRetryCount())
            if (readBytes <= 0)
            {
                // リトライオーバー...
                Log.v(TAG, " RECEIVE : RETRY OVER...... : $delayMs ms x ${command.maxRetryCount()}  SEQ: $sequenceNumber isRetry: ${command.isRetrySend}")
                if (!command.isRetrySend)
                {
                    // 再送しない場合には、応答がないことを通知する
                    receivedAllMessage(isDumpReceiveLog, id, null, callback)
                    return (false)
                }
                return (true)
            }

            // 受信したデータをバッファに突っ込む
            var receivedLength = 0
            val byteStream = ByteArrayOutputStream()
            while (readBytes > 0)
            {
                readBytes = inputStream.read(byteArray, 0, receiveMessageBufferSize)
                if (readBytes <= 0)
                {
                    Log.v(TAG, " RECEIVED MESSAGE FINISHED ($readBytes)")
                    break
                }
                byteStream.write(byteArray, 0, readBytes)
                sleep(delayMs)
                receivedLength += readBytes
                readBytes = inputStream.available()
            }

            Log.v(TAG, " receivedLength : $receivedLength")
            if (receivedLength >= 4)
            {
                val outputStream = cutHeader(byteStream)
                receivedAllMessage(isDumpReceiveLog, id, outputStream.toByteArray(), callback)
            }
            else
            {
                receivedAllMessage(isDumpReceiveLog, id, byteStream.toByteArray(), callback)
            }
            System.gc()
        }
        catch (e: Throwable)
        {
            e.printStackTrace()
            System.gc()
        }
        return false
    }

    private fun receivedAllMessage(isDumpReceiveLog: Boolean, id: Int, body: ByteArray?, callback: IPtpIpCommandCallback?)
    {
        Log.v(TAG, "receivedAllMessage() : " + (body?.size ?: 0) + " bytes.")
        if ((isDumpReceiveLog)&&(body != null))
        {
            // ログに受信メッセージを出力する
            SimpleLogDumper.dump_bytes("RECV[" + body.size + "] ", body)
        }
        callback?.receivedMessage(id, body)
    }

    private fun receiveMulti(command: IPtpIpCommand, delayMs: Int): Boolean
    {
        val isDumpLog = command.dumpLog()
        var maxRetryCount = command.maxRetryCount()
        val id = command.id
        val callback = command.responseCallback()
        try
        {
            // Log.v(TAG, " ===== receive_multi() =====")
            val receiveMessageBufferSize = BUFFER_SIZE
            val byteArray = ByteArray(receiveMessageBufferSize)
            val inputStream = socket?.getInputStream()
            if (inputStream == null)
            {
                Log.v(TAG, " InputStream is NULL... RECEIVE ABORTED.")
                return (false)
            }

            // 初回データが受信バッファにデータが溜まるまで待つ...
            var readBytes = waitForReceive(inputStream, delayMs, command.maxRetryCount())
            if (readBytes <= 0)
            {
                // リトライオーバー...
                Log.v(TAG, " RECEIVE : RETRY OVER...... : $delayMs ms x ${command.maxRetryCount()}  SEQ: $sequenceNumber ")
                if (command.isRetrySend)
                {
                    // 要求を再送する場合、、、ダメな場合は受信待ちとする
                    Log.v(TAG, " --- SEND RETRY ---")
                    return (true)
                }
                callback?.receivedMessage(id, null)
                return (false)
            }

            // 初回データの読み込み...
            var targetLength = parseDataLength(byteArray, readBytes)
            readBytes = inputStream.read(byteArray, 0, receiveMessageBufferSize)
            var receivedLength = readBytes
            if (targetLength <= 0)
            {
                // もう一回データを読み直す...
                targetLength = parseDataLength(byteArray, readBytes)
            }
            if ((targetLength == 0)&&(readBytes > 0))
            {
                // 知らないデータがついている...ダンプしてみる
                // Log.v(TAG, " RECEIVE UNKNOWN BYTES : ${readBytes}")
                if (isDumpLog)
                {
                    // ログに送信メッセージを出力する
                    SimpleLogDumper.dump_bytes("RECV.UNKNOWN[${readBytes}] ", byteArray)
                }
                callback?.receivedMessage(id, null)
                return (false)
            }

            if ((targetLength <= 0)||(readBytes <= 0))
            {
                // 受信サイズ異常の場合...
                if (isDumpLog)
                {
                    if (receivedLength > 0)
                    {
                        SimpleLogDumper.dump_bytes("WRONG DATA : ", byteArray.copyOfRange(0,
                            receivedLength.coerceAtMost(64)
                        ))
                    }
                    Log.v(TAG, " WRONG LENGTH. : $targetLength READ : $receivedLength bytes.")
                }
                callback?.receivedMessage(id, null)

                // 受信したデータが不足しているので、もう一度受信待ち
                Log.v(TAG, " 1st receive : AGAIN. [$readBytes][$targetLength]")
                return (true)
            }

            //  初回データの受信を報告する。
            if (isDumpLog)
            {
                Log.v(TAG, "  -=-=-=- 1st CALL : read_bytes : " + readBytes + "(" + receivedLength + ") : target_length : " + targetLength + "  buffer SIZE : " + byteArray.size)
            }
            callback?.onReceiveProgress(receivedLength, targetLength, byteArray.copyOfRange(fromIndex = 0, toIndex = receivedLength))

            var isWaitLogging = true
            while ((maxRetryCount > 0)&&(readBytes >= 0)&&(receivedLength < targetLength))
            {
                sleep(delayMs)
                readBytes = inputStream.available()
                if (readBytes <= 0)
                {
                    if (isWaitLogging)
                    {
                        Log.v(TAG, " WAIT is.available() ... [length: $receivedLength, target: $targetLength] $readBytes bytes, retry : $maxRetryCount  SEQ: $sequenceNumber")
                        isWaitLogging = false
                    }
                    maxRetryCount--
                    continue
                }
                if (!isWaitLogging)
                {
                    Log.v(TAG, " WAIT FOR RECEIVE COUNT: $maxRetryCount (SEQ: $sequenceNumber)")
                }

                readBytes = inputStream.read(byteArray, 0, receiveMessageBufferSize)
                if (readBytes <= 0)
                {
                    if (isDumpLog)
                    {
                        Log.v(TAG, "  RECEIVED MESSAGE FINISHED ($readBytes) [receivedLength: $receivedLength, targetLength: $targetLength]")
                    }
                    break
                }
                receivedLength += readBytes
                callback?.onReceiveProgress(receivedLength, targetLength, byteArray.copyOfRange(0, readBytes))
                maxRetryCount = command.maxRetryCount()
            }

            // 最後のデータを受信した後にもう一度受信が必要な場合の処理...
            if (command.isLastReceiveRetry)
            {
                var responseReceive = true
                try
                {
                    while (responseReceive)
                    {
                        Log.v(TAG, "   --- isLastReceiveRetry is true ---  SEQ: $sequenceNumber")
                        sleep(delayMs)
                        if (inputStream.available() > 0)
                        {
                            readBytes = inputStream.read(byteArray, 0, receiveMessageBufferSize)
                            if (readBytes > 0)
                            {
                                receivedLength += readBytes
                                callback?.onReceiveProgress(receivedLength, targetLength, byteArray.copyOfRange(0, readBytes))
                                Log.v(TAG, "   --- isLastReceiveRetry: onReceiveProgress() $readBytes bytes. ---  SEQ: $sequenceNumber ")
                            }
                            responseReceive = false
                        }
                        else
                        {
                            Log.v(TAG, "   --- inputStream.available() is <= 0 --- : ${inputStream.available()}  SEQ: $sequenceNumber")
                            responseReceive = false
                        }
                    }
                }
                catch (ex: Exception)
                {
                    ex.printStackTrace()
                }
            }

            //  終了報告...
            if (isDumpLog)
            {
                Log.v(TAG, "  --- receive_multi : $id  ($readBytes) [$maxRetryCount] $receiveMessageBufferSize ($receivedLength) SEQ: $sequenceNumber")
            }
            val copyBytes = if (byteArray.size > receivedLength) { receivedLength } else { byteArray.size }
            callback?.receivedMessage(id, byteArray.copyOfRange(0, copyBytes))
        }
        catch (e: Throwable)
        {
            e.printStackTrace()
        }
        return (false)
    }

    private fun parseDataLength(byteArray: ByteArray, readBytes: Int): Int
    {
        var offset = 0
        var dateLength = 0
        try
        {
            if (readBytes > 20)
            {
                if (byteArray[offset + 4].toUByte().toInt() == 0x07)
                {
                    // 前の応答が入っていると考える... レングスバイト分読み飛ばす
                    offset = byteArray[offset].toUByte().toInt()
                }
                if (byteArray[offset + 4].toUByte().toInt() == 0x09)
                {
                    // データバイト... (Start Data Packet で データレングスを特定する
                    dateLength = (byteArray[offset + 15].toUByte().toInt() and 0xff shl 24) + (byteArray[offset + 14].toUByte().toInt() and 0xff shl 16) + (byteArray[offset + 13].toUByte().toInt() and 0xff shl 8) + (byteArray[offset + 12].toUByte().toInt() and 0xff)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (dateLength)
    }

    private fun cutHeader(receivedBuffer: ByteArrayOutputStream): ByteArrayOutputStream
    {
        try
        {
            val byteArray = receivedBuffer.toByteArray()
            val limit = byteArray.size
            var dataLength = 0
            val len = (byteArray[3].toUByte().toInt() and 0xff shl 24) + (byteArray[2].toUByte().toInt() and 0xff shl 16) + (byteArray[1].toUByte().toInt() and 0xff shl 8) + (byteArray[0].toUByte().toInt() and 0xff)
            val packetType = byteArray[4].toUByte().toInt() and 0xff
            if ((limit == len)||(limit < 16384))
            {
                // 応答は１つしか入っていない。もしくは受信データサイズが16kBの場合は、そのまま返す。
                return (receivedBuffer)
            }

            if (packetType == 0x09)
            {
                dataLength = (byteArray[15].toUByte().toInt() and 0xff shl 24) + (byteArray[14].toUByte().toInt() and 0xff shl 16) + (byteArray[13].toUByte().toInt() and 0xff shl 8) + (byteArray[12].toUByte().toInt() and 0xff)
            }

            if (dataLength == 0)
            {
                // データとしては変なので、なにもしない
                return receivedBuffer
            }
            val outputStream = ByteArrayOutputStream()
            var position = 20 // ヘッダ込の先頭
            while (position < limit)
            {
                dataLength = (byteArray[position + 3].toUByte().toInt() and 0xff shl 24) + (byteArray[position + 2].toUByte().toInt() and 0xff shl 16) + (byteArray[position + 1].toUByte().toInt() and 0xff shl 8) + (byteArray[position].toUByte().toInt() and 0xff)

                val copyByte = (limit - (position + 12)).coerceAtMost(dataLength - 12)
                outputStream.write(byteArray, position + 12, copyByte)
                position += dataLength
            }
            return (outputStream)
        }
        catch (e: Throwable)
        {
            e.printStackTrace()
            System.gc()
        }
        return (receivedBuffer)
    }

    private fun waitForReceive(inputStream : InputStream, delayMs: Int, retryCnt : Int): Int
    {
        var retryCount = retryCnt
        var isLogOutput = true
        var readBytes = 0
        try
        {
            while ((retryCount >= 0) && (readBytes <= 0))
            {
                sleep(delayMs)
                readBytes = inputStream.available()
                if (readBytes <= 0)   // if (readBytes <= 0)
                {
                    if (isLogOutput)
                    {
                        Log.v(TAG, "waitForReceive:: is.available() WAIT... : $delayMs ms (Count : $retryCount/$retryCnt) SEQ: $sequenceNumber")
                        isLogOutput = false
                    }
                    if (!waitForever)
                    {
                        retryCount--
                    }
                    else
                    {
                        Log.v(TAG, "waitForReceive: wait forever ")
                        isLogOutput = false
                    }
                }
            }
            if (!isLogOutput)
            {
                Log.v(TAG, " --- waitForReceive : $readBytes bytes. (RetryCount : $retryCount/$retryCnt)")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (readBytes)
    }

    companion object
    {
        private val TAG = PtpIpCommandPublisher::class.java.simpleName

        private const val SEQUENCE_START_NUMBER = 1
        private const val BUFFER_SIZE = 1024 * 1024 + 16 // 受信バッファは 1MB
        private const val COMMAND_SEND_RECEIVE_DURATION_MS = 5
        private const val COMMAND_SEND_RECEIVE_DURATION_MAX = 3000
        private const val COMMAND_POLL_QUEUE_MS = 5 // 5
    }
}
