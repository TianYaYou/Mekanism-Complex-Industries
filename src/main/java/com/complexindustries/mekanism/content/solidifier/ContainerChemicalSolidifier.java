package com.complexindustries.mekanism.content.solidifier;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ContainerChemicalSolidifier extends MekanismTileContainer<TileEntityChemicalSolidifier> {

    public ContainerChemicalSolidifier(int id, Inventory inv, TileEntityChemicalSolidifier tile) {
        super(MCIContainerTypes.CHEMICAL_SOLIDIFIER, id, inv, tile);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return super.stillValid(player);
    }
}
