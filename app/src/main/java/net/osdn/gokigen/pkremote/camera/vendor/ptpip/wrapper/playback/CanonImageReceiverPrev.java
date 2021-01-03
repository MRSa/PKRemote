package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.playback;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IProgressEvent;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandCanonGetPartialObject;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonRequestInnerDevelopEnd;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.CanonRequestInnerDevelopStart;

import java.io.ByteArrayOutputStream;


public class CanonImageReceiverPrev implements IPtpIpCommandCallback, ICanonImageReceiver
{
    private static final String TAG = CanonImageReceiver.class.getSimpleName();

    private final Activity activity;
    private final IPtpIpCommandPublisher publisher;
    private final IPtpIpCommandCallback mine;
    private IDownloadContentCallback callback = null;
    private int objectId = 0;
    private boolean isReceiveMulti = false;
    private boolean receivedFirstData = false;

    private int received_total_bytes = 0;
    private int received_remain_bytes = 0;

    public CanonImageReceiverPrev(@NonNull Activity activity, @NonNull IPtpIpCommandPublisher publisher)
    {
        this.activity = activity;
        this.publisher = publisher;
        this.mine = this;
    }

    @Override
    public void issueCommand(final int objectId, int imageSize, IDownloadContentCallback callback)
    {
        if (this.objectId != 0)
        {
            // already issued
            Log.v(TAG, " COMMAND IS ALREADY ISSUED. : " + objectId);
            return;
        }
        this.callback = callback;
        this.objectId = objectId;
        publisher.enqueueCommand(new CanonRequestInnerDevelopStart(new IPtpIpCommandCallback() {
            @Override
            public void receivedMessage(int id, byte[] rx_body) {
                Log.v(TAG, " getRequestStatusEvent  : " + objectId + " " + ((rx_body != null) ? rx_body.length : 0));
                publisher.enqueueCommand(new PtpIpCommandGeneric(mine,  (objectId + 5), false, objectId, 0x9116));
            }

            @Override
            public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body) {
            }

            @Override
            public boolean isReceiveMulti() {
                return (false);
            }
        }, objectId, false, objectId, objectId, 0x0f, 0x02));   // 0x9141 : RequestInnerDevelopStart
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            if (id == objectId + 1)
            {
                sendTransferComplete(rx_body);
            }
            else if (id == objectId + 2)
            {
                Log.v(TAG, " requestInnerDevelopEnd() : " + objectId);
                publisher.enqueueCommand(new CanonRequestInnerDevelopEnd(this, (objectId + 3), false, objectId));  // 0x9143 : RequestInnerDevelopEnd
            }
            else if (id == objectId + 3)
            {
                //Log.v(TAG, "  --- COMMAND RESET : " + objectId + " --- ");

                // リセットコマンドを送ってみる
                publisher.enqueueCommand(new PtpIpCommandGeneric(this, (objectId + 4), false, objectId, 0x902f));
            }
            else if (id == objectId + 4)
            {
                // 画像取得終了
                Log.v(TAG, " ----- SMALL IMAGE RECEIVE SEQUENCE FINISHED  : " + objectId);
                callback.onCompleted();
                this.objectId = 0;
                this.callback = null;
                this.received_total_bytes = 0;
                this.received_remain_bytes = 0;
                this.receivedFirstData = false;
                System.gc();
            }
            else if (id == objectId + 5)
            {
                requestGetPartialObject(rx_body);
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
        byte[] body = cutHeader(rx_body);
        int length = (body == null) ? 0 : body.length;
        Log.v(TAG, " onReceiveProgress() " + currentBytes + "/" + totalBytes + " (" + length + " bytes.)");
        callback.onProgress(body, length, new IProgressEvent() {
            @Override
            public float getProgress() {
                return ((float) currentBytes / (float) totalBytes);
            }

            @Override
            public boolean isCancellable() {
                return (false);
            }

            @Override
            public void requestCancellation() {

            }
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
            // 初回データを読み込んだ
            receivedFirstData = true;

            // データを最初に読んだとき。ヘッダ部分を読み飛ばす
            data_position = (int) rx_body[0] & (0xff);
        }
        else if (received_remain_bytes > 0)
        {
            //Log.v(TAG, "  >>> [ remain_bytes : " + received_remain_bytes + "] ( length : " + length + ") " + data_position);
            //SimpleLogDumper.dump_bytes("[zzz]", Arrays.copyOfRange(rx_body, data_position, (data_position + 160)));

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
                Log.v(TAG, "  BODY SIZE IS SMALL : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] " + rx_body.length + "  ");
                //int startpos = (data_position > 48) ? (data_position - 48) : 0;
                //SimpleLogDumper.dump_bytes("[xxx]", Arrays.copyOfRange(rx_body, startpos, (data_position + 48)));
                break;
            }

            // Log.v(TAG, " RX DATA : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] (" + received_total_bytes + ")");
            //SimpleLogDumper.dump_bytes("[yyy] " + data_position + ": ", Arrays.copyOfRange(rx_body, data_position, (data_position + 64)));
            if ((data_position + body_size) > length)
            {
                // データがすべてバッファ内になかったときは、バッファすべてコピーして残ったサイズを記憶しておく。
                int copysize = (length - ((data_position + 12)));
                byteStream.write(rx_body, (data_position + 12), copysize);
                received_remain_bytes = body_size - copysize - 12;  // マイナス12は、ヘッダ分
                received_total_bytes = received_total_bytes + copysize;
               // Log.v(TAG, " --- copy : " + (data_position + 12) + " " + copysize + " remain : " + received_remain_bytes + "  body size : " + body_size);
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

    private void requestGetPartialObject(@Nullable byte[] rx_body)
    {
        Log.v(TAG, " requestGetPartialObject() : " + objectId);
        isReceiveMulti = true;
        receivedFirstData = false;

        // 0x9107 : GetPartialObject  (元は 0x00020000)
        int pictureLength;
        if ((rx_body != null)&&(rx_body.length > 52))
        {
            int dataIndex = 48;
            pictureLength = (rx_body[dataIndex] & 0xff);
            pictureLength = pictureLength + ((rx_body[dataIndex + 1]  & 0xff) << 8);
            pictureLength = pictureLength + ((rx_body[dataIndex + 2] & 0xff) << 16);
            pictureLength = pictureLength + ((rx_body[dataIndex + 3] & 0xff) << 24);
        }
        else
        {
            pictureLength = 0x020000;
        }
        publisher.enqueueCommand(new PtpIpCommandCanonGetPartialObject(this, (objectId + 1), false, objectId, 0x01, 0x00, pictureLength, pictureLength));
    }

    private void sendTransferComplete(byte[] rx_body)
    {
        Log.v(TAG, " sendTransferComplete(), id : " + objectId + " size: " + ((rx_body != null) ? rx_body.length : 0));
        publisher.enqueueCommand(new PtpIpCommandGeneric(this,  (objectId + 2), false, objectId, 0x9117, 4,0x01));  // 0x9117 : TransferComplete
        isReceiveMulti = false;
    }
}
