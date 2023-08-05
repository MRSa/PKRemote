package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.liveview;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener;
import net.osdn.gokigen.pkremote.camera.liveview.CameraLiveViewListenerImpl;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT;
import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT_DEFAULT_VALUE;

public class PtpIpLiveViewControl implements ILiveViewControl, IPtpIpCommunication
{
    private final String TAG = toString();
    private static final int STREAM_PORT_DEFAULT = 15742;   // ??

    private String ipAddress = IPreferencePropertyAccessor.CANON_HOST_IP_DEFAULT_VALUE;
    private int portNumber = STREAM_PORT_DEFAULT;
    private final CameraLiveViewListenerImpl liveViewListener;
    private int waitMs = 0;
    private static final int DATA_HEADER_OFFSET = 18;
    private static final int BUFFER_SIZE = 2048 * 1280;
    private static final int ERROR_LIMIT = 30;
    private boolean isStart = false;
    private final boolean logcat;

    public PtpIpLiveViewControl(@NonNull AppCompatActivity activity, boolean logcat)
    {
        //this.ipAddress = ip;
        //this.portNumber = portNumber;
        this.logcat = logcat;
        liveViewListener = new CameraLiveViewListenerImpl();

        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            String waitMsStr = preferences.getString(FUJIX_LIVEVIEW_WAIT, FUJIX_LIVEVIEW_WAIT_DEFAULT_VALUE);
            logcat("waitMS : " + waitMsStr);
            int wait = Integer.parseInt(waitMsStr);
            if ((wait >= 20)&&(wait <= 800))
            {
                waitMs = wait;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            waitMs = 100;
        }
        Log.v(TAG, "LOOP WAIT : " + waitMs + " ms");
    }

    @Override
    public void startLiveView(boolean isCameraScreen)
    {
        if (isStart)
        {
            // すでに受信スレッド動作中なので抜ける
            return;
        }
        isStart = true;
        Thread thread = new Thread(() -> {
            try
            {
                Socket socket = new Socket(ipAddress, portNumber);
                startReceive(socket);
            }
            catch (Exception e)
            {
                Log.v(TAG, " IP : " + ipAddress + " port : " + portNumber);
                e.printStackTrace();
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
    public void stopLiveView()
    {
        isStart = false;
    }

    private void startReceive(Socket socket)
    {
        String lvHeader = "[LV]";
        int lvHeaderDumpBytes = 24;

        int errorCount = 0;
        InputStream isr;
        byte[] byteArray;
        try
        {
            isr = socket.getInputStream();
            byteArray = new byte[BUFFER_SIZE + 32];
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.v(TAG, "===== startReceive() aborted.");
            return;
        }
        while (isStart)
        {
            try
            {
                boolean findJpeg = false;
                int length_bytes;
                int read_bytes = isr.read(byteArray, 0, BUFFER_SIZE);
                if (read_bytes > DATA_HEADER_OFFSET)
                {
                    // メッセージボディの先頭にあるメッセージ長分は読み込む
                    length_bytes = ((((int) byteArray[3]) & 0xff) << 24) + ((((int) byteArray[2]) & 0xff) << 16) + ((((int) byteArray[1]) & 0xff) << 8) + (((int) byteArray[0]) & 0xff);
                    if ((byteArray[18] == (byte)0xff)&&(byteArray[19] == (byte)0xd8))
                    {
                        findJpeg = true;
                        while ((read_bytes < length_bytes) && (read_bytes < BUFFER_SIZE) && (length_bytes <= BUFFER_SIZE))
                        {
                            int append_bytes = isr.read(byteArray, read_bytes, length_bytes - read_bytes);
                            logcat("READ AGAIN : " + append_bytes + " [" + read_bytes + "]");
                            if (append_bytes < 0)
                            {
                                break;
                            }
                            read_bytes = read_bytes + append_bytes;
                        }
                        logcat("READ BYTES : " + read_bytes + "  (" + length_bytes + " bytes, " + waitMs + "ms)");
                    }
                    else
                    {
                        // ウェイトを短めに入れてマーカーを拾うまで待つ
                        Thread.sleep(waitMs/4);
                        continue;
                    }
                }

                // 先頭データ(24バイト分)をダンプ
                dump_bytes(lvHeader, byteArray, lvHeaderDumpBytes);

                if (findJpeg)
                {
                    liveViewListener.onUpdateLiveView(Arrays.copyOfRange(byteArray, DATA_HEADER_OFFSET, read_bytes - DATA_HEADER_OFFSET), null);
                    errorCount = 0;
                }
                Thread.sleep(waitMs);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                errorCount++;
            }
            if (errorCount > ERROR_LIMIT)
            {
                // エラーが連続でたくさん出たらループをストップさせる
                isStart = false;
            }
        }
        try
        {
            isr.close();
            socket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void updateDigitalZoom()
    {

    }

    @Override
    public void updateMagnifyingLiveViewScale(boolean isChangeScale)
    {

    }

    @Override
    public float getMagnifyingLiveViewScale()
    {
        return (1.0f);
    }

    @Override
    public void changeLiveViewSize(String size)
    {

    }

    @Override
    public float getDigitalZoomScale()
    {
        return (1.0f);
    }

    public ILiveViewListener getLiveViewListener()
    {
        return (liveViewListener);
    }

    @Override
    public boolean connect(@NonNull String ipAddress, int portNumber)
    {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        return (true);
    }

    @Override
    public void disconnect()
    {
        isStart = false;
    }

    /**
     *   デバッグ用：ログにバイト列を出力する
     *
     */
    private void dump_bytes(String header, byte[] data, int dumpBytes)
    {
        if (!logcat)
        {
            // ログ出力しないモードだった
            return;
        }

        int index = 0;
        if (dumpBytes <= 0)
        {
            dumpBytes = 24;
        }
        StringBuilder message = new StringBuilder();
        for (int point = 0; point < dumpBytes; point++)
        {
            byte item = data[point];
            index++;
            message.append(String.format("%02x ", item));
            if (index >= 8)
            {
                Log.v(TAG, header + " " + message);
                index = 0;
                message = new StringBuilder();
            }
        }
        if (index != 0)
        {
            Log.v(TAG, header + " " + message);
        }
        System.gc();
    }

    private void logcat(String message)
    {
        if (logcat)
        {
            Log.v(TAG, message);
        }
    }
}
