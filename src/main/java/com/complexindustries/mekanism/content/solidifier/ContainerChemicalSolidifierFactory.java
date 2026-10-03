package com.complexindustries.mekanism.content.solidifier;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tier.FactoryTier;
import net.minecraft.world.entity.player.Inventory;

public class ContainerChemicalSolidifierFactory extends MekanismTileContainer<TileEntityChemicalSolidifierFactory> {

    public ContainerChemicalSolidifierFactory(int id, Inventory inv, TileEntityChemicalSolidifierFactory tile) {
        super(MCIContainerTypes.CHEMICAL_SOLIDIFIER_FACTORY, id, inv, tile);
    }

    @Override
    protected int getInventoryYOffset() {
        return 98;
    }

    @Override
    protected int getInventoryXOffset() {
        return tile.tier == FactoryTier.ULTIMATE ? 26 : 8;
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return super.stillValid(player);
    }
}
