package net.osdn.gokigen.pkremote;

import androidx.annotation.Nullable;

public interface ICardSlotSelector
{
    void setupSlotSelector(boolean isEnable, @Nullable ICardSlotSelectionReceiver slotSelectionReceiver);

}
