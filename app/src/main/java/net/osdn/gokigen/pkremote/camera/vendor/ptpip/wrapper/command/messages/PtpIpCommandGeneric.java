package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;


public class PtpIpCommandGeneric extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;
    private final int bodySize;
    private final int id;

    private final byte opCode0;
    private final byte opCode1;

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

    private final byte dataC;
    private final byte dataD;
    private final byte dataE;
    private final byte dataF;

    public PtpIpCommandGeneric(@NonNull IPtpIpCommandCallback callback, int id, int opcode)
    {
        this.callback = callback;
        this.bodySize = 0;

        this.id = id;
        opCode0 = ((byte) (0x000000ff & opcode));
        opCode1 = ((byte)((0x0000ff00 & opcode) >> 8));

        data0 = 0;
        data1 = 0;
        data2 = 0;
        data3 = 0;

        data4 = 0;
        data5 = 0;
        data6 = 0;
        data7 = 0;

        data8 = 0; // ((byte) (0x000000ff & value3));
        data9 = 0; // ((byte)((0x0000ff00 & value3) >> 8));
        dataA = 0; // ((byte)((0x00ff0000 & value3) >> 16));
        dataB = 0; // ((byte)((0xff000000 & value3) >> 24));

        dataC = 0; // ((byte) (0x000000ff & value4));
        dataD = 0; // ((byte)((0x0000ff00 & value4) >> 8));
        dataE = 0; // ((byte)((0x00ff0000 & value4) >> 16));
        dataF = 0; // ((byte)((0xff000000 & value4) >> 24));
    }

    public PtpIpCommandGeneric(@NonNull IPtpIpCommandCallback callback, int id, int opcode, int bodySize, int value)
    {
        this.callback = callback;
        this.bodySize = bodySize;

        this.id = id;
        opCode0 = ((byte) (0x000000ff & opcode));
        opCode1 = ((byte)((0x0000ff00 & opcode) >> 8));

        data0 = ((byte) (0x000000ff & value));
        data1 = ((byte)((0x0000ff00 & value) >> 8));
        data2 = ((byte)((0x00ff0000 & value) >> 16));
        data3 = ((byte)((0xff000000 & value) >> 24));

        data4 = ((byte) (0x000000ff & value));
        data5 = ((byte)((0x0000ff00 & value) >> 8));
        data6 = ((byte)((0x00ff0000 & value) >> 16));
        data7 = ((byte)((0xff000000 & value) >> 24));

        data8 = 0; // ((byte) (0x000000ff & value3));
        data9 = 0; // ((byte)((0x0000ff00 & value3) >> 8));
        dataA = 0; // ((byte)((0x00ff0000 & value3) >> 16));
        dataB = 0; // ((byte)((0xff000000 & value3) >> 24));

        dataC = 0; // ((byte) (0x000000ff & value4));
        dataD = 0; // ((byte)((0x0000ff00 & value4) >> 8));
        dataE = 0; // ((byte)((0x00ff0000 & value4) >> 16));
        dataF = 0; // ((byte)((0xff000000 & value4) >> 24));
    }

    public PtpIpCommandGeneric(@NonNull IPtpIpCommandCallback callback, int id, int opcode, int bodySize, int value, int value2)
    {
        this.callback = callback;
        this.bodySize = bodySize;

        this.id = id;
        opCode0 = ((byte) (0x000000ff & opcode));
        opCode1 = ((byte)((0x0000ff00 & opcode) >> 8));

        data0 = ((byte) (0x000000ff & value));
        data1 = ((byte)((0x0000ff00 & value) >> 8));
        data2 = ((byte)((0x00ff0000 & value) >> 16));
        data3 = ((byte)((0xff000000 & value) >> 24));

        data4 = ((byte) (0x000000ff & value2));
        data5 = ((byte)((0x0000ff00 & value2) >> 8));
        data6 = ((byte)((0x00ff0000 & value2) >> 16));
        data7 = ((byte)((0xff000000 & value2) >> 24));

        data8 = 0; // ((byte) (0x000000ff & value3));
        data9 = 0; // ((byte)((0x0000ff00 & value3) >> 8));
        dataA = 0; // ((byte)((0x00ff0000 & value3) >> 16));
        dataB = 0; // ((byte)((0xff000000 & value3) >> 24));

        dataC = 0; // ((byte) (0x000000ff & value4));
        dataD = 0; // ((byte)((0x0000ff00 & value4) >> 8));
        dataE = 0; // ((byte)((0x00ff0000 & value4) >> 16));
        dataF = 0; // ((byte)((0xff000000 & value4) >> 24));
    }

    public PtpIpCommandGeneric(@NonNull IPtpIpCommandCallback callback, int id, int opcode, int bodySize, int value, int value2, int value3)
    {
        this.callback = callback;
        this.bodySize = bodySize;

        this.id = id;
        opCode0 = ((byte) (0x000000ff & opcode));
        opCode1 = ((byte)((0x0000ff00 & opcode) >> 8));

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

        dataC = 0; // ((byte) (0x000000ff & value4));
        dataD = 0; // ((byte)((0x0000ff00 & value4) >> 8));
        dataE = 0; // ((byte)((0x00ff0000 & value4) >> 16));
        dataF = 0; // ((byte)((0xff000000 & value4) >> 24));

    }

    public PtpIpCommandGeneric(@NonNull IPtpIpCommandCallback callback, int id, int opcode, int bodySize, int value, int value2, int value3, int value4)
    {
        this.callback = callback;
        this.bodySize = bodySize;

        this.id = id;
        opCode0 = ((byte) (0x000000ff & opcode));
        opCode1 = ((byte)((0x0000ff00 & opcode) >> 8));

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

        dataC = ((byte) (0x000000ff & value4));
        dataD = ((byte)((0x0000ff00 & value4) >> 8));
        dataE = ((byte)((0x00ff0000 & value4) >> 16));
        dataF = ((byte)((0xff000000 & value4) >> 24));
    }

    @Override
    public IPtpIpCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (id);
    }

    @Override
    public byte[] commandBody()
    {
        if (bodySize == 2)
        {
            return (new byte[]{

                    // packet type
                    (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // data phase info
                    (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // operation code
                    opCode0, opCode1,

                    // sequence number
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // data ...
                    data0, data1,
            });
        }
        else if (bodySize == 4)
        {
            return (new byte[]{

                    // packet type
                    (byte) 0x06, (byte) 0x00,  (byte) 0x00, (byte) 0x00,

                    // data phase info
                    (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // operation code
                    opCode0, opCode1,

                    // sequence number
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // data ...
                    data0, data1, data2, data3,
            });
        }
        else if (bodySize == 8)
        {
            return (new byte[]{

                    // packet type
                    (byte) 0x06, (byte) 0x00,  (byte) 0x00, (byte) 0x00,

                    // data phase info
                    (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // operation code
                    opCode0, opCode1,

                    // sequence number
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // data ...
                    data0, data1, data2, data3,
                    data4, data5, data6, data7,
            });
        }
        else if (bodySize == 12)
        {
            return (new byte[]{

                    // packet type
                    (byte) 0x06, (byte) 0x00,  (byte) 0x00, (byte) 0x00,

                    // data phase info
                    (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // operation code
                    opCode0, opCode1,

                    // sequence number
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // data ...
                    data0, data1, data2, data3,
                    data4, data5, data6, data7,
                    data8, data9, dataA, dataB,
            });
        }
        else if (bodySize == 16)
        {
            return (new byte[]{

                    // packet type
                    (byte) 0x06, (byte) 0x00,  (byte) 0x00, (byte) 0x00,

                    // data phase info
                    (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // operation code
                    opCode0, opCode1,

                    // sequence number
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // data ...
                    data0, data1, data2, data3,
                    data4, data5, data6, data7,
                    data8, data9, dataA, dataB,
                    dataC, dataD, dataE, dataF,
            });
        }
        else //  ボディ長が 2, 4, 8, 12 以外の場合... (ボディなし)
        {
            return (new byte[]{

                    // packet type
                    (byte) 0x06, (byte) 0x00,  (byte) 0x00, (byte) 0x00,

                    // data phase info
                    (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // operation code
                    opCode0, opCode1,

                    // sequence number
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            });
        }
    }
}
