package com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.states;

import com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.AutoDrillRefuel;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import net.minecraft.client.Minecraft;

public class AbiphoneState implements AutoDrillRefuelState{
    private final Minecraft mc = Minecraft.getMinecraft();
    private int retryTicks = 0;
    private int retries = 0;
    private static final int MAX_RETRIES = 3;
    private boolean clicked = false;


    @Override
    public void onStart(AutoDrillRefuel refueler) {
        int abiphoneSlot = InventoryUtil.getHotbarSlotOfItem("Abiphone");
        if (abiphoneSlot == -1) {
            logError("No abiphone found!");
            refueler.stop();
            refueler.setError(AutoDrillRefuel.AutoDrillRefuelError.NO_ABIPHONE);
            return;
        }

        mc.thePlayer.inventory.currentItem = abiphoneSlot;
        clicked = true;
        KeyBindUtil.rightClick();
        retryTicks = 10; // wait ~0.5s before checking GUI
        log("Entering abiphone state");
    }

    @Override
    @Override
public AutoDrillRefuelState onTick(AutoDrillRefuel refueler) {
    if (retryTicks > 0) {
        retryTicks--;
        return this;
    }

    if (InventoryUtil.getInventoryName().contains("Abiphone") && InventoryUtil.isInventoryLoaded()) {
        log("Opened Abiphone GUI");
        int greatforgeSlot = InventoryUtil.getSlotIdOfItemInContainer("Greatforge");

        if (greatforgeSlot == -1) {
            logError("No Greatforge contact found");
            refueler.stop();
            refueler.setError(AutoDrillRefuel.AutoDrillRefuelError.NO_GREATFORGE_CONTACT);
            return null;
        }

        InventoryUtil.clickContainerSlot(greatforgeSlot, 0, InventoryUtil.ClickMode.PICKUP);
        return new GreatforgeState();
    }

    // Retry logic if GUI did not open
    if (clicked && retries < MAX_RETRIES) {
        retries++;
        log("Retrying Abiphone use (" + retries + "/" + MAX_RETRIES + ")");
        KeyBindUtil.rightClick();
        retryTicks = 10;
        return this;
    }

    // After max retries
    if (retries >= MAX_RETRIES) {
        logError("Failed to open Abiphone GUI after retries");
        refueler.stop();
        refueler.setError(AutoDrillRefuel.AutoDrillRefuelError.NO_ABIPHONE);
        return null;
    }
    
        return this;
    }

    @Override
    public void onEnd(AutoDrillRefuel refueler) {
        log("Ending abiphone state");
    }
}
