package net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.connection;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.IFujiXMessages;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.StatusRequestMessage;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.changemode.ChangeToPlayback1st;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.changemode.ChangeToPlayback2nd;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.changemode.ChangeToPlayback3rd;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.command.messages.changemode.ChangeToPlayback4th;

public class FujiXCameraModeChangeToPlayback implements View.OnClickListener, IFujiXCommandCallback, IFujiXMessages
{
    private final String TAG = toString();
    private final IFujiXCommandPublisher publisher;
    private final IFujiXCommandCallback callback;

    public FujiXCameraModeChangeToPlayback(@NonNull IFujiXCommandPublisher publisher, @Nullable IFujiXCommandCallback callback)
    {
        this.publisher = publisher;
        this.callback = callback;
    }

    @Override
    public void onClick(View v)
    {
        Log.v(TAG, "onClick");
        try
        {
            publisher.enqueueCommand(new ChangeToPlayback1st(this));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        //Log.v(TAG, "receivedMessage : " + id + "[" + rx_body.length + " bytes]");
        //int bodyLength = 0;
        try
        {
            switch (id)
            {
                case SEQ_CHANGE_TO_PLAYBACK_1ST:
                    publisher.enqueueCommand(new ChangeToPlayback2nd(this));
                    break;

                case SEQ_CHANGE_TO_PLAYBACK_2ND:
                    publisher.enqueueCommand(new ChangeToPlayback3rd(this));
                    break;

                case SEQ_CHANGE_TO_PLAYBACK_3RD:
                    publisher.enqueueCommand(new ChangeToPlayback4th(this));
                    break;

                case SEQ_CHANGE_TO_PLAYBACK_4TH:
                    publisher.enqueueCommand(new StatusRequestMessage(this));
                    break;

                case SEQ_STATUS_REQUEST:
                    if (callback != null)
                    {
                        callback.receivedMessage(id, rx_body);
                    }
                    Log.v(TAG, "CHANGED PLAYBACK MODE : DONE.");
                    break;

                default:
                    Log.v(TAG, "RECEIVED UNKNOWN ID : " + id);
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
