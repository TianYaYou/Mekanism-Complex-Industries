package com.complexindustries.mekanism.content.chamber;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ContainerCrystalGrowthChamber extends MekanismTileContainer<TileEntityCrystalGrowthChamber> {

    public ContainerCrystalGrowthChamber(int id, Inventory inv, TileEntityCrystalGrowthChamber tile) {
        super(MCIContainerTypes.CRYSTAL_GROWTH_CHAMBER, id, inv, tile);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return super.stillValid(player);
    }
}
