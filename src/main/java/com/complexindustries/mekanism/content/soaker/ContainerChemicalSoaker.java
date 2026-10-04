package com.complexindustries.mekanism.content.soaker;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ContainerChemicalSoaker extends MekanismTileContainer<TileEntityChemicalSoaker> {

    public ContainerChemicalSoaker(int id, Inventory inv, TileEntityChemicalSoaker tile) {
        super(MCIContainerTypes.CHEMICAL_SOAKER, id, inv, tile);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return super.stillValid(player);
    }
}
