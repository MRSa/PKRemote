package net.osdn.gokigen.pkremote.transfer;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public interface ITransferMessage
{
    void storedImage(@NonNull String filename, Bitmap picture);
    void showInformation(@NonNull String message);
}
