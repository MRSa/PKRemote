package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IProgressEvent
import net.osdn.gokigen.pkremote.camera.utils.SimpleLogDumper
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandCanonGetPartialObject
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonRequestInnerDevelopEnd
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonRequestInnerDevelopStart
import java.io.ByteArrayOutputStream
import java.util.*

class CanonImageReceiver(private val activity: AppCompatActivity, private val publisher: IPtpIpCommandPublisher, private val sequenceType : Int) : IPtpIpCommandCallback, ICanonImageReceiver
{
    private val mine = this
    private val isDumpLog = false

    private var callback: IDownloadContentCallback? = null
    private var objectId = 0
    private var isReceiveMulti = false
    private var receivedFirstData = false
    private var receivedTotalBytes = 0
    private var receivedRemainBytes = 0

    override fun issueCommand(objectId: Int, imageSize: Int, callback: IDownloadContentCallback?)
    {
        Log.v(TAG, " issueCommand() : $objectId (size:$imageSize)")
        if (this.objectId != 0)
        {
            // already issued
            Log.v(TAG, " COMMAND IS ALREADY ISSUED. : $objectId")
            return
        }
        this.callback = callback
        this.objectId = objectId
        publisher.enqueueCommand(PtpIpCommandGeneric(this, objectId + 7, isDumpLog, objectId, 0x902f))
    }

    override fun receivedMessage(id: Int, rxBody: ByteArray?)
    {
        try
        {
            when (id)
            {
                (objectId + 7) -> {
                    if (sequenceType == 1)
                    {
                        publisher.enqueueCommand(CanonRequestInnerDevelopStart(this, objectId + 8, isDumpLog, objectId, objectId, 0x06, 0x02))  // 0x9141 : RequestInnerDevelopStart
                    }
                    else
                    {
                        publisher.enqueueCommand(CanonRequestInnerDevelopStart(this, objectId + 8, isDumpLog, objectId, objectId, 0x0f, 0x02))  // 0x9141 : RequestInnerDevelopStart
                    }
                }
                (objectId + 1) -> {
                    sendTransferComplete(rxBody)
                }
                (objectId + 2) -> {
                    Log.v(TAG, " requestInnerDevelopEnd() : $objectId")
                    publisher.enqueueCommand(CanonRequestInnerDevelopEnd(this, objectId + 3, isDumpLog, objectId)) // 0x9143 : RequestInnerDevelopEnd
                }
                (objectId + 3) -> {
                    // リセットコマンドを送ってみる
                    publisher.enqueueCommand(PtpIpCommandGeneric(this, objectId + 4, isDumpLog, objectId, 0x902f))
                }
                (objectId + 4) -> {
                    // 画像取得終了
                    Log.v(TAG, " ----- SMALL IMAGE RECEIVE SEQUENCE FINISHED  : $objectId")
                    callback?.onCompleted()
                    objectId = 0
                    callback = null
                    receivedTotalBytes = 0
                    receivedRemainBytes = 0
                    receivedFirstData = false
                    System.gc()
                }
                (objectId + 5) -> {
                    requestGetPartialObject(rxBody)
                }
                (objectId + 8) -> {
                    checkReplyInnerDevelopStart(rxBody)
                }
                else -> {
                    Log.v(TAG, " RECEIVED UNKNOWN ID : $id")
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            callback?.onErrorOccurred(e)
        }
    }

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, rxBody: ByteArray?)
    {
        val body = cutHeader(rxBody)
        val length = body?.size ?: 0
        Log.v(TAG, " onReceiveProgress() $currentBytes/$totalBytes ($length bytes.)")
        callback?.onProgress(body, length, object : IProgressEvent
        {
            override fun getProgress(): Float { return (currentBytes.toFloat() / totalBytes.toFloat()) }
            override fun isCancellable(): Boolean { return (false) }
            override fun requestCancellation() { }
        })
    }

    private fun cutHeader(rxBody: ByteArray?): ByteArray?
    {
        if (rxBody == null)
        {
            return (null)
        }
        val length = rxBody.size
        var dataPosition = 0
        val byteStream = ByteArrayOutputStream()
        if (!receivedFirstData)
        {
            // 初回データを読み込んだ
            receivedFirstData = true

            // データを最初に読んだとき。ヘッダ部分を読み飛ばす
            dataPosition = rxBody[0].toUByte().toInt() and 0xff
        }
        else if (receivedRemainBytes > 0)
        {
            //Log.v(TAG, "  >>> [ remain_bytes : " + received_remain_bytes + "] ( length : " + length + ") " + data_position);
            //SimpleLogDumper.dump_bytes("[zzz]", Arrays.copyOfRange(rx_body, data_position, (data_position + 160)));

            // データの読み込みが途中だった場合...
            if (length < receivedRemainBytes)
            {
                // 全部コピーする、足りないバイト数は残す
                receivedRemainBytes = receivedRemainBytes - length
                receivedTotalBytes = receivedTotalBytes + rxBody.size
                return rxBody
            }
            else
            {
                byteStream.write(rxBody, dataPosition, receivedRemainBytes)
                dataPosition = receivedRemainBytes
                receivedRemainBytes = 0
            }
        }
        while (dataPosition <= length - 12)
        {
            val bodySize: Int = (rxBody[dataPosition].toUByte().toInt() and 0xff) + (rxBody[dataPosition + 1].toUByte().toInt() and 0xff shl 8) + (rxBody[dataPosition + 2].toUByte().toInt() and 0xff shl 16) + (rxBody[dataPosition + 3].toUByte().toInt() and 0xff shl 24)
            if (bodySize <= 12)
            {
                Log.v(TAG, "  BODY SIZE IS SMALL : " + dataPosition + " (" + bodySize + ") [" + receivedRemainBytes + "] " + rxBody.size + "  ")
                //int startpos = (data_position > 48) ? (data_position - 48) : 0;
                //SimpleLogDumper.dump_bytes("[xxx]", Arrays.copyOfRange(rx_body, startpos, (data_position + 48)));
                break
            }

            // Log.v(TAG, " RX DATA : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] (" + received_total_bytes + ")");
            //SimpleLogDumper.dump_bytes("[yyy] " + data_position + ": ", Arrays.copyOfRange(rx_body, data_position, (data_position + 64)));
            if (dataPosition + bodySize > length)
            {
                // データがすべてバッファ内になかったときは、バッファすべてコピーして残ったサイズを記憶しておく。
                val copySize = length - (dataPosition + 12)
                byteStream.write(rxBody, dataPosition + 12, copySize)
                receivedRemainBytes = bodySize - copySize - 12 // マイナス12は、ヘッダ分
                receivedTotalBytes = receivedTotalBytes + copySize
                // Log.v(TAG, " --- copy : " + (data_position + 12) + " " + copySize + " remain : " + received_remain_bytes + "  body size : " + body_size);
                break
            }
            try
            {
                byteStream.write(rxBody, dataPosition + 12, bodySize - 12)
                dataPosition = dataPosition + bodySize
                receivedTotalBytes = receivedTotalBytes + 12
                //Log.v(TAG, " --- COPY : " + (data_position + 12) + " " + (body_size - 12) + " remain : " + received_remain_bytes);
            }
            catch (e: Exception)
            {
                Log.v(TAG, "  pos : $dataPosition  size : $bodySize length : $length")
                e.printStackTrace()
            }
        }
        return byteStream.toByteArray()
    }

    override fun isReceiveMulti(): Boolean
    {
        return (isReceiveMulti)
    }

    private fun checkReplyInnerDevelopStart(rxBody: ByteArray?)
    {
        Log.v(TAG, " getRequestStatusEvent  : $objectId (" + (rxBody?.size ?: 0) + ") ")
        publisher.enqueueCommand(PtpIpCommandGeneric(mine, objectId + 5, isDumpLog, objectId, 0x9116)) // Event Receive
    }

    private fun requestGetPartialObject(rxBody: ByteArray?) {
        Log.v(TAG, " requestGetPartialObject() : $objectId")
        if (rxBody != null)
        {
            SimpleLogDumper.dump_bytes(" requestGetPartialObject ", Arrays.copyOfRange(rxBody, 0, 64))
        }
        isReceiveMulti = true
        receivedFirstData = false

        // 0x9107 : GetPartialObject  (元は 0x00020000)
        var pictureLength: Int
        if (rxBody != null && rxBody.size > 52)
        {
            val dataIndex = 48
            pictureLength = (rxBody[dataIndex].toUByte().toInt() and 0xff) + (rxBody[dataIndex + 1].toUByte().toInt() and 0xff shl 8) + (rxBody[dataIndex + 2].toUByte().toInt() and 0xff shl 16) + (rxBody[dataIndex + 3].toUByte().toInt() and 0xff shl 24)
        }
        else
        {
            pictureLength = 0x020000
        }
        if ((pictureLength <= 0)&&(pictureLength > 0x020000))
        {
            pictureLength = 0x020000
        }
        Log.v(TAG, " requestGetPartialObject()  size : $pictureLength ")
        publisher.enqueueCommand(PtpIpCommandCanonGetPartialObject(this, objectId + 1, isDumpLog, objectId, 0x01, 0x00, pictureLength, pictureLength))
    }

    private fun sendTransferComplete(rxBody: ByteArray?)
    {
        Log.v(TAG, " sendTransferComplete(), id : $objectId size: " + (rxBody?.size ?: 0))
        publisher.enqueueCommand(PtpIpCommandGeneric(this, objectId + 2, isDumpLog, objectId, 0x9117, 4, 0x01)) // 0x9117 : TransferComplete
        isReceiveMulti = false
    }

    companion object
    {
        private val TAG = CanonImageReceiver::class.java.simpleName
    }
}
