package net.osdn.gokigen.pkremote.camera.vendor.nikon.wrapper.playback;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IProgressEvent;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;

import java.io.ByteArrayOutputStream;

import static net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpMessages.GET_PARTIAL_OBJECT;

public class NikonSmallImageReceiver implements IPtpIpCommandCallback
{
    private static final String TAG = NikonFullImageReceiver.class.getSimpleName();

    private final IPtpIpCommandPublisher publisher;
    private IDownloadContentCallback callback = null;

    private boolean isReceiveMulti = true;
    private int objectId = 0;

    private int received_total_bytes = 0;
    private int received_remain_bytes = 0;

    private int target_image_size = 0;
    private boolean receivedFirstData = false;
    private int delayMs;

    NikonSmallImageReceiver(@NonNull IPtpIpCommandPublisher publisher, int delayMs)
    {
        this.publisher = publisher;
        this.delayMs = delayMs;
    }

    void issueCommand(int objectId, IDownloadContentCallback callback)
    {
        if (this.objectId != 0)
        {
            // already issued
            Log.v(TAG, " COMMAND IS ALREADY ISSUED. : " + objectId);
            return;
        }
        this.callback = callback;
        this.objectId = objectId;
        this.isReceiveMulti = true;
        this.receivedFirstData = false;
        //Log.v(TAG, " getPartialObject (id : " + objectId + ", size:" + imageSize + ")");
        //publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_PARTIAL_OBJECT, true, 0, 0x101b, 12, objectId, 0, imageSize));  // GetPartialObject 0x101b
        publisher.enqueueCommand(new PtpIpCommandGeneric(this, GET_PARTIAL_OBJECT, delayMs, true, 0, 0x90c4, 4, objectId, 0, 0, 0));  // GetLargeThumb
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            if (id == GET_PARTIAL_OBJECT)
            {
                Log.v(TAG, " TransferComplete() RECEIVED  : " + id + " (" + objectId + ") size : " + target_image_size);

                // end of receive sequence.
                callback.onCompleted();
                isReceiveMulti = false;
                receivedFirstData = false;
                received_remain_bytes = 0;
                received_total_bytes = 0;
                target_image_size = 0;
                objectId = 0;
                callback = null;
                System.gc();
            }
            else
            {
                Log.v(TAG, " RECEIVED UNKNOWN ID : " + id);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            {
                callback.onErrorOccurred(e);
            }
        }
    }

    @Override
    public void onReceiveProgress(final int currentBytes, final int totalBytes, byte[] rx_body)
    {
        // 受信したデータから、通信のヘッダ部分を削除する
        byte[] body = cutHeader(rx_body);
        int length = (body == null) ? 0 : body.length;
        Log.v(TAG, " onReceiveProgress() " + currentBytes + "/" + totalBytes + " (" + length + " bytes.)");
        callback.onProgress(body, length, new IProgressEvent() {
            @Override
            public float getProgress() {
                return ((float) currentBytes / (float) target_image_size);
            }

            @Override
            public boolean isCancellable() {
                return (false);
            }

            @Override
            public void requestCancellation() { }
        });
    }

    private byte[] cutHeader(byte[] rx_body)
    {
        if (rx_body == null)
        {
            return (null);
        }
        int length = rx_body.length;
        int data_position = 0;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        if (!receivedFirstData)
        {
            // データを最初に読んだとき。ヘッダ部分を読み飛ばす
            receivedFirstData = true;
            data_position = (int) rx_body[0] & (0xff);
            Log.v(TAG, " FIRST DATA POS. : " + data_position);
            //SimpleLogDumper.dump_bytes(" [sss]", Arrays.copyOfRange(rx_body, 0, (64)));
        }
        else if (received_remain_bytes > 0)
        {
            // データの読み込みが途中だった場合...
            if (length < received_remain_bytes)
            {
                // 全部コピーする、足りないバイト数は残す
                received_remain_bytes = received_remain_bytes - length;
                received_total_bytes = received_total_bytes + rx_body.length;
                return (rx_body);
            }
            else
            {
                byteStream.write(rx_body, data_position, received_remain_bytes);
                data_position = received_remain_bytes;
                received_remain_bytes = 0;
            }
        }

        while (data_position <= (length - 12))
        {
            int body_size =  (rx_body[data_position] & 0xff) + ((rx_body[data_position + 1]  & 0xff) << 8) +
                    ((rx_body[data_position + 2] & 0xff) << 16) + ((rx_body[data_position + 3] & 0xff) << 24);
            if (body_size <= 12)
            {
                Log.v(TAG, " --- BODY SIZE IS SMALL : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] " + rx_body.length + "  (" + target_image_size + ")");
                //int startpos = (data_position > 48) ? (data_position - 48) : 0;
                //SimpleLogDumper.dump_bytes(" [xxx]", Arrays.copyOfRange(rx_body, startpos, (data_position + 48)));
                break;
            }

            // 受信データ(のヘッダ部分)をダンプする
            //Log.v(TAG, " RX DATA : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] (" + received_total_bytes + ")");
            //SimpleLogDumper.dump_bytes(" [zzz] " + data_position + ": ", Arrays.copyOfRange(rx_body, data_position, (data_position + 48)));

            if ((data_position + body_size) > length)
            {
                // データがすべてバッファ内になかったときは、バッファすべてコピーして残ったサイズを記憶しておく。
                int copysize = (length - ((data_position + 12)));
                byteStream.write(rx_body, (data_position + 12), copysize);
                received_remain_bytes = body_size - copysize - 12;  // マイナス12は、ヘッダ分
                received_total_bytes = received_total_bytes + copysize;
                //Log.v(TAG, " ----- copy : " + (data_position + 12) + " " + copysize + " remain : " + received_remain_bytes + "  body size : " + body_size);
                break;
            }
            try
            {
                byteStream.write(rx_body, (data_position + 12), (body_size - 12));
                data_position = data_position + body_size;
                received_total_bytes = received_total_bytes + 12;
                //Log.v(TAG, " --- COPY : " + (data_position + 12) + " " + (body_size - 12) + " remain : " + received_remain_bytes);

            }
            catch (Exception e)
            {
                Log.v(TAG, "  pos : " + data_position + "  size : " + body_size + " length : " + length);
                e.printStackTrace();
            }
        }
        return (byteStream.toByteArray());
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (isReceiveMulti);
    }
}
