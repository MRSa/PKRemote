package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.connection

import android.app.Activity
import android.graphics.Color
import android.util.Log
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.IPtpIpInterfaceProvider
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status.PtpIpStatusChecker

class CanonCameraConnectSequenceForPlaybackType1(val context: Activity, val cameraStatusReceiver: ICameraStatusReceiver, val cameraConnection: ICameraConnection, val interfaceProvider: IPtpIpInterfaceProvider, val statusChecker: PtpIpStatusChecker) : Runnable, IPtpIpCommandCallback, IPtpIpMessages
{
    private val isDumpLog = true
    private val commandIssuer = interfaceProvider.commandPublisher
    //private var requestMessageCount = 0

    override fun run()
    {
        try
        {
            Log.v(TAG, " CanonCameraConnectSequenceForPlaybackType1 START!")

            // カメラとTCP接続
            val issuer = interfaceProvider.commandPublisher
            if (!issuer.isConnected)
            {
                if (!interfaceProvider.commandCommunication.connect())
                {
                    // 接続失敗...
                    interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.dialog_title_connect_failed_canon), false, true, Color.RED)
                    cameraConnection.alertConnectingFailed(context.getString(R.string.dialog_title_connect_failed_canon))
                    return
                }
            }
            else
            {
                Log.v(TAG, "SOCKET IS ALREADY CONNECTED...")
            }
            // コマンドタスクの実行開始
            issuer.start()

            // 接続シーケンスの開始
            sendRegistrationMessage()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.dialog_title_connect_failed_canon), false, true, Color.RED)
            cameraConnection.alertConnectingFailed(e.message)
        }
    }

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, body: ByteArray?)
    {
        Log.v(TAG, " $currentBytes/$totalBytes")
    }

    override fun isReceiveMulti(): Boolean
    {
        return false
    }

    //@ExperimentalUnsignedTypes
    override fun receivedMessage(id: Int, rx_body: ByteArray)
    {
        when (id)
        {
            IPtpIpMessages.SEQ_REGISTRATION -> if (checkRegistrationMessage(rx_body)) {
                sendInitEventRequest(rx_body)
            } else {
                cameraConnection.alertConnectingFailed(context.getString(R.string.connect_error_message))
            }
            IPtpIpMessages.SEQ_EVENT_INITIALIZE -> if (checkEventInitialize(rx_body)) {
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting1), false, false, 0)
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_OPEN_SESSION, isDumpLog, 0, 0x1002, 4, 0x41))
            } else {
                cameraConnection.alertConnectingFailed(context.getString(R.string.connect_error_message))
            }
            IPtpIpMessages.SEQ_OPEN_SESSION -> {
                Log.v(TAG, " SEQ_OPEN_SESSION ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting2), false, false, 0)
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_INIT_SESSION, isDumpLog, 0, 0x902f))
            }
            IPtpIpMessages.SEQ_INIT_SESSION -> {
                Log.v(TAG, " SEQ_INIT_SESSION ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting3), false, false, 0)
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_CHANGE_REMOTE, isDumpLog, 0, 0x9114, 4, 0x15))
            }
            IPtpIpMessages.SEQ_CHANGE_REMOTE -> {
                Log.v(TAG, " SEQ_CHANGE_REMOTE ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting4), false, false, 0)
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_SET_EVENT_MODE, isDumpLog, 0, 0x9115, 4, 0x02))
            }
            IPtpIpMessages.SEQ_SET_EVENT_MODE -> {
                Log.v(TAG, " SEQ_SET_EVENT_MODE ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting5), false, false, 0)
                Log.v(TAG, " SEQ_DEVICE_PROPERTY_FINISHED ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.connect_connect_finished), false, false, 0)
                connectFinished()
                Log.v(TAG, "CHANGED MODE : DONE.")
            }
            else -> {
                Log.v(TAG, "RECEIVED UNKNOWN ID : $id")
                cameraConnection.alertConnectingFailed(context.getString(R.string.connect_receive_unknown_message))
            }
        }
    }

    private fun sendRegistrationMessage()
    {
        Log.v(TAG, " sendRegistrationMessage() ")

        interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.connect_start), false, false, 0)
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start))
        //commandIssuer.enqueueCommand(CanonRegistrationMessage(this))
    }

    private fun sendInitEventRequest(receiveData: ByteArray)
    {
        interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.connect_start_2), false, false, 0)
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start_2))
        try
        {
            var eventConnectionNumber: Int = receiveData[8].toUByte().toInt() and 0xff
            eventConnectionNumber += (receiveData[9].toUByte().toInt() and 0xff shl 8)
            eventConnectionNumber += (receiveData[10].toUByte().toInt() and 0xff shl 16)
            eventConnectionNumber += (receiveData[11].toUByte().toInt() and 0xff shl 24)
            statusChecker.setEventConnectionNumber(eventConnectionNumber)
            interfaceProvider.cameraStatusWatcher.startStatusWatch(interfaceProvider.statusListener)
            commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_OPEN_SESSION, isDumpLog, 0, 0x1002, 4, 0x41))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkRegistrationMessage(receiveData: ByteArray?): Boolean
    {
        // データ(Connection Number)がないときにはエラーと判断する
        return !(receiveData == null || receiveData.size < 12)
    }

    private fun checkEventInitialize(receiveData: ByteArray?): Boolean
    {
        Log.v(TAG, "checkEventInitialize() ")
        return receiveData != null
    }

    private fun connectFinished()
    {
        try
        {
            // 接続成功のメッセージを出す
            interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.connect_connected), false, false, 0)

            // ちょっと待つ
            Thread.sleep(1000)

            // 接続成功！のメッセージを出す
            interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.connect_connected), false, false, 0)
            onConnectNotify()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun onConnectNotify()
    {
        try
        {
            val thread = Thread {
                // カメラとの接続確立を通知する
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected))
                cameraStatusReceiver.onCameraConnected()
                Log.v(TAG, " onConnectNotify()")
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private const val TAG = "CanonConnectSeq.1"
    }
}
