package net.osdn.gokigen.pkremote;

public interface IInformationReceiver
{
    void updateMessage(final String message, final boolean isBold, final boolean isColor,  final int color);
}
