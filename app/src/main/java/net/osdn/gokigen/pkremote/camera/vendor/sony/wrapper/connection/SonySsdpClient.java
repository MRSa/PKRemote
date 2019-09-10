package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCamera;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.SonyCameraDeviceProvider;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *  Sony SSDP Client : SonyのCameraRemoteSampleApp にある SimpleSsdpClient を参考にインプリメントした
 *   (API Level 14を minSdkVersion に設定したので... NsdManager.DiscoveryListener を使わなかった)
 *
 *    SSDP : Simple Service Discovery Protocol
 *
 */
class SonySsdpClient
{
    private final String TAG = toString();
    private static final int SEND_TIMES_DEFAULT = 3;
    private static final int SEND_WAIT_DURATION_MS = 100;
    private static final int SSDP_RECEIVE_TIMEOUT = 3 * 1000; // msec
    private static final int PACKET_BUFFER_SIZE = 2048;
    private static final int SSDP_PORT = 1900;
    private static final int SSDP_MX = 1;
    private static final String SSDP_ADDR = "239.255.255.250";
    private static final String SSDP_ST = "urn:schemas-sony-com:service:ScalarWebAPI:1";
    private static final String SSDP_ST2 = "urn:schemas-upnp-org:service:ContentDirectory:1";
    // private static final String SSDP_ST2 = "urn:schemas-upnp-org:device:MediaServer:1";
    private final Context context;
    private final ISearchResultCallback callback;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final String ssdpRequest;
    private final int sendRepeatCount;
    private final boolean useSmartphoneTransfer;

    SonySsdpClient(@NonNull Context context, @NonNull ISearchResultCallback callback, @NonNull ICameraStatusReceiver statusReceiver, int sendRepeatCount)
    {
        this.context = context;
        this.callback = callback;
        this.cameraStatusReceiver = statusReceiver;
        this.sendRepeatCount = (sendRepeatCount >= 0) ? sendRepeatCount : SEND_TIMES_DEFAULT;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        useSmartphoneTransfer = preferences.getBoolean(IPreferencePropertyAccessor.USE_SMARTPHONE_TRANSFER_MODE, false);
        ssdpRequest = "M-SEARCH * HTTP/1.1\r\n"
                + String.format(Locale.US, "HOST: %s:%d\r\n", SSDP_ADDR, SSDP_PORT)
                + "MAN: \"ssdp:discover\"\r\n"
                + String.format(Locale.US, "MX: %d\r\n", SSDP_MX)
                + String.format("ST: %s\r\n", (useSmartphoneTransfer ? SSDP_ST2 : SSDP_ST)) + "\r\n";
    }

    void search()
    {
        final byte[] sendData = ssdpRequest.getBytes();
        String detailString = "";
        DatagramSocket socket = null;
        DatagramPacket receivePacket;
        DatagramPacket packet;

        //  要求の送信
        try
        {
            socket = new DatagramSocket();
            socket.setReuseAddress(true);
            InetSocketAddress iAddress = new InetSocketAddress(SSDP_ADDR, SSDP_PORT);
            packet = new DatagramPacket(sendData, sendData.length, iAddress);

            // 要求を繰り返し送信する
            for (int loop = 1; loop <= sendRepeatCount; loop++)
            {
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_search_request) + " " + loop);
                socket.send(packet);
                Thread.sleep(SEND_WAIT_DURATION_MS);
            }
        }
        catch (Exception e)
        {
            if ((socket != null) && (!socket.isClosed()))
            {
                socket.close();
            }
            e.printStackTrace();

            // エラー応答する
            callback.onErrorFinished(detailString + " : " + e.getLocalizedMessage());
            return;
        }

        // 応答の受信
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        List<String> foundDevices = new ArrayList<>();
        byte[] array = new byte[PACKET_BUFFER_SIZE];
        try
        {
            cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_wait_reply));
            while (currentTime - startTime < SSDP_RECEIVE_TIMEOUT)
            {
                receivePacket = new DatagramPacket(array, array.length);
                socket.setSoTimeout(SSDP_RECEIVE_TIMEOUT);
                socket.receive(receivePacket);
                String ssdpReplyMessage = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                String ddUsn;
                if (ssdpReplyMessage.contains("HTTP/1.1 200"))
                {
                    ddUsn = findParameterValue(ssdpReplyMessage, "USN");
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_received_reply));
                    if (!foundDevices.contains(ddUsn))
                    {
                        String ddLocation = findParameterValue(ssdpReplyMessage, "LOCATION");
                        foundDevices.add(ddUsn);

                        //// Fetch Device Description XML and parse it.
                        if (ddLocation != null)
                        {
                            cameraStatusReceiver.onStatusNotify("LOCATION : " + ddLocation);
                            ISonyCamera device = SonyCameraDeviceProvider.searchSonyCameraDevice(ddLocation);
                            if ((device != null) && (device.hasApiService("camera")))
                            {
                                cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_found) + " " + device.getFriendlyName());
                                callback.onDeviceFound(device);
                                // カメラが見つかった場合は breakする
                                break;
                            }
                            else
                            {
                                if ((useSmartphoneTransfer)&&(device != null))
                                {
                                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_found) + " " + device.getFriendlyName() + " (Smartphone Transfer)");
                                    Log.v(TAG, " SMARTPHONE TRANSFER : " + ssdpReplyMessage);
                                    callback.onDeviceFound(device);
                                    break;
                                }

                                // カメラが見つからない...
                                cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_not_found));
                            }
                        }
                    }
                    else
                    {
                        Log.v(TAG, "Already received. : " + ddUsn);
                    }
                }
                else
                {
                    Log.v(TAG, " SSDP REPLY (ignored) : " + ssdpReplyMessage + " " + useSmartphoneTransfer);
                }
                currentTime = System.currentTimeMillis();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

            // エラー応答する
            callback.onErrorFinished(detailString + " : " + e.getLocalizedMessage());
            return;
        }
        finally
        {
            try
            {
                if (!socket.isClosed())
                {
                    socket.close();
                }
            } catch (Exception ee)
            {
                ee.printStackTrace();
            }
        }
        callback.onFinished();
    }

    private static String findParameterValue(@NonNull String ssdpMessage, @NonNull String paramName)
    {
        String name = paramName;
        if (!name.endsWith(":"))
        {
            name = name + ":";
        }
        int start = ssdpMessage.indexOf(name);
        int end = ssdpMessage.indexOf("\r\n", start);
        if ((start != -1)&&(end != -1))
        {
            start += name.length();
            try
            {
                return ((ssdpMessage.substring(start, end)).trim());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return (null);
    }

    /**
     *   検索結果のコールバック
     *
     */
    public interface ISearchResultCallback
    {
        void onDeviceFound(ISonyCamera cameraDevice);   // デバイスが見つかった！
        void onFinished();                              // 通常の終了をしたとき
        void onErrorFinished(String reason);            // エラーが発生して応答したとき
    }
}
