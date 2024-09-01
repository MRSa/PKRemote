package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener;
import net.osdn.gokigen.pkremote.camera.liveview.CameraLiveViewListenerImpl;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class PanasonicLiveViewControl implements ILiveViewControl
{
    private final String TAG = toString();
    private final IPanasonicCamera camera;
    //private final BlockingQueue<byte[]> mJpegQueue = new ArrayBlockingQueue<>(2);
    private final CameraLiveViewListenerImpl liveViewListener;
    private DatagramSocket receiveSocket = null;
    private boolean whileStreamReceive = false;
    private int errorOccur = 0;
    private static final int TIMEOUT_MAX = 3;
    private static final int ERROR_MAX = 30;
    private static final int RECEIVE_BUFFER_SIZE = 1024 * 1024 * 4;
    private static final int TIMEOUT_MS = 1500;
    private static final int LIVEVIEW_PORT = 49152;
    private final String LIVEVIEW_START_REQUEST = "cam.cgi?mode=startstream&value=49152";
    private final String LIVEVIEW_STOP_REQUEST = "cam.cgi?mode=stopstream";

    PanasonicLiveViewControl(@NonNull IPanasonicCamera camera)
    {
        this.camera = camera;
        liveViewListener = new CameraLiveViewListenerImpl();
    }

    @Override
    public void changeLiveViewSize(String size)
    {

    }

    @Override
    public void startLiveView(final boolean isCameraScreen)
    {
        Log.v(TAG, "startLiveView() : " + isCameraScreen);
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        startReceiveStream();
                        if (!whileStreamReceive)
                        {
                            Log.v(TAG, "CANNOT OPEN : UDP RECEIVE SOCKET");
                            return;
                        }
                        String requestUrl = camera.getCmdUrl() + LIVEVIEW_START_REQUEST;
                        String reply = SimpleHttpClient.httpGet(requestUrl, TIMEOUT_MS);
                        if (!reply.contains("<result>ok</result>"))
                        {
                            try
                            {
                                // エラー回数のカウントアップ
                                errorOccur++;

                                // 少し待つ...
                                Thread.sleep(TIMEOUT_MS);

                                if (errorOccur < ERROR_MAX)
                                {
                                    Log.v(TAG, "RETRY START LIVEVIEW... : " + errorOccur);
                                    startLiveView(isCameraScreen);
                                }
                                else
                                {
                                    Log.v(TAG, "RETRY OVER : START LIVEVIEW");
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            Log.v(TAG, "   ----- START LIVEVIEW ----- : " + requestUrl);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
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
        Log.v(TAG, "stopLiveView()");
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String reply = SimpleHttpClient.httpGet(camera.getCmdUrl() + LIVEVIEW_STOP_REQUEST, TIMEOUT_MS);
                        if (!reply.contains("<result>ok</result>"))
                        {
                            Log.v(TAG, "stopLiveview() reply is fail... " + reply);
                        }
                        else
                        {
                            Log.v(TAG, "stopLiveview() is issued.");
                        }
                        //  ライブビューウォッチャーを止める
                        whileStreamReceive = false;
                        closeReceiveSocket();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
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
    public float getDigitalZoomScale()
    {
        return (1.0f);
    }

    private void startReceiveStream()
    {
        if (whileStreamReceive)
        {
            Log.v(TAG, "startReceiveStream() : already starting.");
            return;
        }

        // ソケットをあける (UDP)
        try
        {
            receiveSocket = new DatagramSocket(LIVEVIEW_PORT);
            whileStreamReceive = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            whileStreamReceive = false;
            receiveSocket = null;
        }

        // 受信スレッドを動かす
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiverThread();
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

    private void checkReceiveImage(@NonNull DatagramPacket packet)
    {
        int dataLength = packet.getLength();
        int searchIndex = 0;
        int startPosition = 0;
        int[] startmarker = { 0xff, 0xd8 };
        byte[] receivedData = packet.getData();
        if (receivedData == null)
        {
            // 受信データが取れなかったので終了する
            Log.v(TAG, "RECEIVED DATA IS NULL...");
            return;
        }
        //Log.v(TAG, "RECEIVED PACKET : " + dataLength);
        while (startPosition < dataLength)
        {
            // 先頭のjpegマーカーが出てくるまで読み飛ばす
            try
            {
                if (receivedData[startPosition++] == (byte) startmarker[searchIndex])
                {
                    searchIndex++;
                    if (searchIndex >= startmarker.length)
                    {
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
        }
        int offset = startPosition - startmarker.length;
        //liveViewListener.onUpdateLiveView(Arrays.copyOfRange(receivedData, offset, dataLength - offset), null);
        liveViewListener.onUpdateLiveView(Arrays.copyOfRange(receivedData, offset, dataLength), null);
    }

    private void receiverThread()
    {
        int exceptionCount = 0;
        byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];
        while (whileStreamReceive)
        {
            try
            {
                DatagramPacket receive_packet = new DatagramPacket(buffer, buffer.length);
                if (receiveSocket != null)
                {
                    receiveSocket.setSoTimeout(TIMEOUT_MS);
                    receiveSocket.receive(receive_packet);
                    checkReceiveImage(receive_packet);
                    exceptionCount = 0;
                }
                else
                {
                    Log.v(TAG, "receiveSocket is NULL...");
                }
            }
            catch (Exception e)
            {
                exceptionCount++;
                e.printStackTrace();
                if (exceptionCount > TIMEOUT_MAX)
                {
                    try
                    {
                        Log.v(TAG, "LV : RETRY REQUEST");

                        exceptionCount = 0;
                        String reply = SimpleHttpClient.httpGet(camera.getCmdUrl() + LIVEVIEW_START_REQUEST, TIMEOUT_MS);
                        if (!reply.contains("ok"))
                        {
                            Log.v(TAG, "LV : RETRY COMMAND FAIL...");
                        }
                    }
                    catch (Exception ee)
                    {
                        ee.printStackTrace();
                    }
                }
            }
        }
        closeReceiveSocket();
        Log.v(TAG, "  ----- startReceiveStream() : Finished.");
        System.gc();
    }

    public ILiveViewListener getLiveViewListener()
    {
        return (liveViewListener);
    }

    private void closeReceiveSocket()
    {
        Log.v(TAG, "closeReceiveSocket()");
        try
        {
            if (receiveSocket != null)
            {
                Log.v(TAG, "  ----- SOCKET CLOSE -----  ");
                receiveSocket.close();
                receiveSocket = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
