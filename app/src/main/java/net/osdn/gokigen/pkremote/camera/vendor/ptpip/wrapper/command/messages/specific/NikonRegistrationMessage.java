package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.PtpIpCommandBase;

import java.nio.ByteBuffer;
import java.util.UUID;

public class NikonRegistrationMessage extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;

    public NikonRegistrationMessage(@NonNull IPtpIpCommandCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public IPtpIpCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (SEQ_REGISTRATION);
    }

    @Override
    public boolean receiveAgainShortLengthMessage()
    {
        return (false);
    }

    @Override
    public boolean useSequenceNumber()
    {
        return (false);
    }

    @Override
    public boolean isIncrementSeqNumber()
    {
        return (false);
    }

    @Override
    public int receiveDelayMs()
    {
        return (80);
    }

    @Override
    public byte[] commandBody()
    {
        int uuid = UUID.randomUUID().hashCode();

        byte[] typeArray = {
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00,
        };

        byte[] uuidArray = {
                (byte)0xad, (byte)0xa5, (byte)0x48, (byte)0x5d, (byte)0x87, (byte)0xb2, (byte)0x7f, (byte)0x0b,
                (byte)0xd3, (byte)0xd5, (byte)0xde, (byte)0xd0, (byte)0x12, (byte)0x44, (byte)0x99, (byte)0x32,
        };

        byte[] deviceNameArray = {
                // device_name 'GOKIGEN_a01'
                (byte)0x47, (byte)0x00, (byte)0x4f, (byte)0x00, (byte)0x4b, (byte)0x00, (byte)0x49, (byte)0x00,
                (byte)0x47, (byte)0x00, (byte)0x45, (byte)0x00, (byte)0x4e, (byte)0x00, (byte)0x5f, (byte)0x00,
                (byte)0x61, (byte)0x00, (byte)0x30, (byte)0x00, (byte)0x31, (byte)0x00, (byte)0x00, (byte)0x00,
        };
        byte[] versionArray = {
                //
                (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00,
        };

        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + 16 + 24 + 4);
        byteBuffer.put(typeArray);
        byteBuffer.put(uuidArray);
        byteBuffer.put(deviceNameArray);
        byteBuffer.put(versionArray);

        return (byteBuffer.array());
    }
}
