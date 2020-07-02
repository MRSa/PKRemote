package net.osdn.gokigen.pkremote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ICardSlotSelector
{
    void setupSlotSelector(boolean isEnable, @Nullable ICardSlotSelectionReceiver slotSelectionReceiver);
    void selectSlot(@NonNull String slotId);
    void changedCardSlot(@NonNull String slotId);
}
