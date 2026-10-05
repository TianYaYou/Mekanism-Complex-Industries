package com.complexindustries.mekanism.content.lithography;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ContainerPhotolithographyMachine extends MekanismTileContainer<TileEntityPhotolithographyMachine> {

    public ContainerPhotolithographyMachine(int id, Inventory inv, TileEntityPhotolithographyMachine tile) {
        super(MCIContainerTypes.PHOTOLITHOGRAPHY_MACHINE, id, inv, tile);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return super.stillValid(player);
    }
}
