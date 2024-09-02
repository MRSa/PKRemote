package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.connection

import android.content.Context
import android.util.Log
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.PanasonicCameraDeviceProvider
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.security.MessageDigest

/**
 * Panasonic SSDP Client : SonyのCameraRemoteSampleApp にある SimpleSsdpClient を参考にインプリメントした
 * (API Level 14を minSdkVersion に設定したので... NsdManager.DiscoveryListener を使わなかった)
 *
 * SSDP : Simple Service Discovery Protocol
 *
 */
class PanasonicSsdpClient(private val context: Context, private val callback: ISearchResultCallback, private val cameraStatusReceiver: ICameraStatusReceiver, sendRepeatCount: Int)
{
    private val ssdpRequest : String = "M-SEARCH * HTTP/1.1\r\nHOST: $SSDP_ADDR:$SSDP_PORT\r\nMAN: \"ssdp:discover\"\r\nMX: $SSDP_MX\r\nST: $SSDP_ST\r\n\r\n"
    private val sendRepeatCount: Int = if ((sendRepeatCount >= 0)) sendRepeatCount else SEND_TIMES_DEFAULT

/*
    init {
        Log.v(TAG, "SSDP REQUEST : $ssdpRequest")
    }
*/

    fun search()
    {
        val sendData = ssdpRequest.toByteArray()
        val detailString = ""
        var socket: DatagramSocket? = null
        var receivePacket: DatagramPacket
        val packet: DatagramPacket
        var isUseDigest = false
        var foundDevice = false
        var device: IPanasonicCamera? = null

        //  要求の送信
        try
        {
            socket = DatagramSocket()
            socket.reuseAddress = true
            val iAddress = InetSocketAddress(SSDP_ADDR, SSDP_PORT)
            packet = DatagramPacket(sendData, sendData.size, iAddress)

            // 要求を繰り返し送信する
            for (loop in 1..sendRepeatCount)
            {
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_search_request) + " " + loop)
                socket.send(packet)
                Thread.sleep(SEND_WAIT_DURATION_MS.toLong())
            }
        }
        catch (e: Exception)
        {
            if ((socket != null) && (!socket.isClosed))
            {
                socket.close()
            }
            e.printStackTrace()
            callback.onErrorFinished(detailString + " : " + e.localizedMessage)
            return
        }

        // ----- 応答の受信
        val startTime = System.currentTimeMillis()
        var currentTime = startTime
        val foundDevices: MutableList<String?> = ArrayList()
        val array = ByteArray(PACKET_BUFFER_SIZE)
        try
        {
            cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_wait_reply))
            while (currentTime - startTime < SSDP_RECEIVE_TIMEOUT)
            {
                receivePacket = DatagramPacket(array, array.size)
                socket.soTimeout = SSDP_RECEIVE_TIMEOUT
                socket.receive(receivePacket)
                val ssdpReplyMessage = String(receivePacket.data, 0, receivePacket.length, charset("UTF-8"))
                var ddUsn: String?
                if (ssdpReplyMessage.contains("HTTP/1.1 200")) {
                    ddUsn = findParameterValue(ssdpReplyMessage, "USN")
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_received_reply))
                    if (!foundDevices.contains(ddUsn)) {
                        val ddLocation = findParameterValue(ssdpReplyMessage, "LOCATION")
                        foundDevices.add(ddUsn)

                        //// Fetch Device Description XML and parse it.
                        if (ddLocation != null)
                        {
                            cameraStatusReceiver.onStatusNotify("LOCATION : $ddLocation")
                            device = PanasonicCameraDeviceProvider.searchPanasonicCameraDevice(ddLocation)
                            if (device != null)
                            {
                                cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_found) + " " + device.getFriendlyName())

                                ///// カメラへの登録要求... /////
                                var retryTimeout = 3
                                var registUrl = device.getCmdUrl() + "cam.cgi?mode=accctrl&type=req_acc&value=" + device.getClientDeviceUuId() + "&value2=GOKIGEN_a01Series"
                                var reply = SimpleHttpClient.httpGet(registUrl, SSDP_RECEIVE_TIMEOUT)
                                Log.v(TAG, " [req_acc] : $reply")

                                //// 新プロトコルへの対応
                                if (!reply.contains("ok"))
                                {
                                    registUrl = device.getCmdUrl() + "cam.cgi?mode=accctrl&type=req_acc_a&value=" + device.getClientDeviceUuId() + "&value2=GOKIGEN_a01Series"
                                    reply = SimpleHttpClient.httpGet(registUrl, SSDP_RECEIVE_TIMEOUT)
                                    Log.v(TAG, " [req_acc_a] : $reply")

                                    if (!reply.contains("ok"))
                                    {
                                        val digest = MessageDigest.getInstance("MD5")
                                        val value1 = digest.digest(device.getClientDeviceUuId().toByteArray()).joinToString("") { "%02x".format(it) }
                                        val value2 = digest.digest("GOKIGEN_a01Series".toByteArray()).joinToString("") { "%02x".format(it) }

                                        // ----- 軽く問い合わせ : req_acc_g → req_acc_e の順で呼び出し必要ぽい
                                        registUrl = "${device.getCmdUrl()}cam.cgi?mode=accctrl&type=req_acc_g&value=${device.getClientDeviceUuId()}&value2=GOKIGEN_a01Series"
                                        val replyG = SimpleHttpClient.httpGet(registUrl, SSDP_RECEIVE_TIMEOUT)
                                        Log.v(TAG, " [req_acc_g] $registUrl : $replyG")

                                        registUrl = "${device.getCmdUrl()}cam.cgi?mode=accctrl&type=req_acc_e&value=${value1}&value2=${value2}"
                                        reply = SimpleHttpClient.httpGet(registUrl, SSDP_RECEIVE_TIMEOUT)
                                        Log.v(TAG, " [req_acc_e] $registUrl : $reply")
                                        isUseDigest = true
                                    }
                                }

                                while ((retryTimeout > 0) && (reply.contains("ok_under_research_no_msg")))
                                {
                                    try
                                    {
                                        // 1秒待って再送してみる
                                        Thread.sleep(1000)
                                        Log.v(TAG, " RETRY SEND : $registUrl")
                                    }
                                    catch (e: Exception)
                                    {
                                        e.printStackTrace()
                                    }
                                    reply = SimpleHttpClient.httpGet(registUrl, SSDP_RECEIVE_TIMEOUT)
                                    retryTimeout--
                                }
                                if ((reply.contains("ok"))&&(reply.contains("remote")))
                                {
                                    val replyList = reply.split(",")
                                    val sessionId = if (replyList.size > 4) { replyList[4].trim() } else { "" }
                                    Log.v(TAG, " RETRY REPLY [OK] ($sessionId) : $reply ")
                                    if (sessionId.isNotEmpty())
                                    {
                                        device.setCommunicationSessionId(sessionId)
                                    }
                                    foundDevice = true
                                    callback.onDeviceFound(device)
                                    // カメラと接続できた場合は breakする
                                    break
                                }
                                // 接続(デバイス登録)エラー...
                                cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_rejected))
                            }
                            else
                            {
                                // カメラが見つからない...
                                cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_not_found))
                            }
                        }
                    }
                    else
                    {
                        Log.v(TAG, "Already received. : $ddUsn")
                    }
                }
                else
                {
                    Log.v(TAG, " SSDP REPLY MESSAGE (ignored) : $ssdpReplyMessage")
                }
                currentTime = System.currentTimeMillis()
            }
            if ((foundDevice)&&(isUseDigest)&&(device != null))
            {
                // ---- デバイスを登録する
                val reply0 = entryDeviceToCamera(device)
                Log.v(TAG, " [setsetting] : $reply0")

                // ---- RAWファイルを転送する設定
                val reply1 = setRawTransferMode(device)
                Log.v(TAG, " [rawTransfer] : $reply1")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            callback.onErrorFinished(detailString + " : " + e.localizedMessage)
            return
        }
        finally
        {
            try
            {
                if (!socket.isClosed)
                {
                    socket.close()
                }
            }
            catch (ee: Exception)
            {
                ee.printStackTrace()
            }
        }
        callback.onFinished()
    }

    private fun entryDeviceToCamera(device: IPanasonicCamera) : String
    {
        try
        {
            val entryUrl = "${device.getCmdUrl()}cam.cgi?mode=setsetting&type=device_name&value=GOKIGEN_a01Series"
            val sessionId = device.getCommunicationSessionId()
            val entryReply = if (!sessionId.isNullOrEmpty())
            {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["X-SESSION_ID"] = sessionId
                SimpleHttpClient.httpGetWithHeader(entryUrl, headerMap, null, SSDP_RECEIVE_TIMEOUT)
            }
            else
            {
                SimpleHttpClient.httpGet(entryUrl, SSDP_RECEIVE_TIMEOUT)
            }
            return (entryReply)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("")
    }

    private fun setRawTransferMode(device: IPanasonicCamera) : String
    {
        try
        {
            val entryUrl = "${device.getCmdUrl()}cam.cgi?mode=setsetting&type=raw_img_send&value=enable"
            val sessionId = device.getCommunicationSessionId()
            val entryReply = if (!sessionId.isNullOrEmpty())
            {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["X-SESSION_ID"] = sessionId
                SimpleHttpClient.httpGetWithHeader(entryUrl, headerMap, null, SSDP_RECEIVE_TIMEOUT)
            }
            else
            {
                SimpleHttpClient.httpGet(entryUrl, SSDP_RECEIVE_TIMEOUT)
            }
            return (entryReply)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("")
    }

    /**
     * 検索結果のコールバック
     *
     */
    interface ISearchResultCallback
    {
        fun onDeviceFound(cameraDevice: IPanasonicCamera?) // デバイスが見つかった！
        fun onFinished() // 通常の終了をしたとき
        fun onErrorFinished(reason: String?) // エラーが発生して応答したとき
    }

    companion object
    {
        private val TAG = PanasonicSsdpClient::class.java.simpleName

        private const val SEND_TIMES_DEFAULT = 3
        private const val SEND_WAIT_DURATION_MS = 100
        private const val SSDP_RECEIVE_TIMEOUT = 4 * 1000 // 4000ms -> 4sec
        private const val PACKET_BUFFER_SIZE = 2048
        private const val SSDP_PORT = 1900
        private const val SSDP_MX = 2
        private const val SSDP_ADDR = "239.255.255.250"
        private const val SSDP_ST = "urn:schemas-upnp-org:device:MediaServer:1"

        private fun findParameterValue(ssdpMessage: String, paramName: String): String? {
            var name = paramName
            if (!name.endsWith(":"))
            {
                name = "$name:"
            }
            var start = ssdpMessage.indexOf(name)
            val end = ssdpMessage.indexOf("\r\n", start)
            if ((start != -1) && (end != -1))
            {
                start += name.length
                try
                {
                    return (ssdpMessage.substring(start, end).trim { it <= ' ' })
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            return (null)
        }
    }
}
