package com.complexindustries.mekanism.content.soaker;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tier.FactoryTier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ContainerChemicalSoakingFactory extends MekanismTileContainer<TileEntityChemicalSoakingFactory> {

    public ContainerChemicalSoakingFactory(int id, Inventory inv, TileEntityChemicalSoakingFactory tile) {
        super(MCIContainerTypes.CHEMICAL_SOAKING_FACTORY, id, inv, tile);
    }

    @Override
    protected int getInventoryYOffset() {
        return 98;
    }

    @Override
    protected int getInventoryXOffset() {
        return tile.tier == FactoryTier.ULTIMATE ? 43 : tile.tier == FactoryTier.ELITE ? 23 : 8;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return super.stillValid(player);
    }
}
