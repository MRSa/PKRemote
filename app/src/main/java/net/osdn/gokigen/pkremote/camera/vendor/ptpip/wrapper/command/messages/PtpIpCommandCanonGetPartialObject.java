package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;

public class PtpIpCommandCanonGetPartialObject extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;
    private final boolean isDumpLog;
    private final int id;
    private final int holdId;
    private final int estimatedObjectSize;

    private final byte data0;
    private final byte data1;
    private final byte data2;
    private final byte data3;

    private final byte data4;
    private final byte data5;
    private final byte data6;
    private final byte data7;

    private final byte data8;
    private final byte data9;
    private final byte dataA;
    private final byte dataB;

    public PtpIpCommandCanonGetPartialObject(@NonNull IPtpIpCommandCallback callback, int id, boolean isDumpLog, int holdId, int value, int value2, int value3, int estimatedObjectSize)
    {
        this.callback = callback;
        this.isDumpLog = isDumpLog;
        this.estimatedObjectSize = estimatedObjectSize;

        this.id = id;
        this.holdId = holdId;

        data0 = ((byte) (0x000000ff & value));
        data1 = ((byte)((0x0000ff00 & value) >> 8));
        data2 = ((byte)((0x00ff0000 & value) >> 16));
        data3 = ((byte)((0xff000000 & value) >> 24));

        data4 = ((byte) (0x000000ff & value2));
        data5 = ((byte)((0x0000ff00 & value2) >> 8));
        data6 = ((byte)((0x00ff0000 & value2) >> 16));
        data7 = ((byte)((0xff000000 & value2) >> 24));

        data8 = ((byte) (0x000000ff & value3));
        data9 = ((byte)((0x0000ff00 & value3) >> 8));
        dataA = ((byte)((0x00ff0000 & value3) >> 16));
        dataB = ((byte)((0xff000000 & value3) >> 24));
    }

    @Override
    public IPtpIpCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int estimatedReceiveDataSize()
    {
        return (estimatedObjectSize);
    }

    @Override
    public int getId()
    {
        return (id);
    }

    @Override
    public int receiveDelayMs()
    {
        return (20);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[]{
                // packet type
                (byte) 0x06, (byte) 0x00,  (byte) 0x00, (byte) 0x00,

                // data phase info
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // operation code
                (byte) 0x07,  (byte) 0x91,

                // sequence number
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // data ...
                data0, data1, data2, data3,
                data4, data5, data6, data7,
                data8, data9, dataA, dataB,
        });
    }

    @Override
    public int getHoldId()
    {
        return (holdId);
    }

    @Override
    public boolean dumpLog()
    {
        return (isDumpLog);
    }

}
