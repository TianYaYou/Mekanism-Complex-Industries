package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.content.blocktype.BlockTypeTile;

import java.util.EnumSet;

public final class MCIBlockTypes {

    public static final BlockTypeTile<TileEntityCrudeOilExtractor> CRUDE_OIL_EXTRACTOR = BlockTypeTile.BlockTileBuilder
            .createBlock(() -> MCIBlockEntityTypes.CRUDE_OIL_EXTRACTOR, MCILang.CRUDE_OIL_EXTRACTOR_DESCRIPTION)
            .withEnergyConfig(() -> FloatingLong.createConst(8_000), () -> FloatingLong.createConst(160_000))
            .withGui(() -> MCIMenuTypes.CRUDE_OIL_EXTRACTOR)
            .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.STONE_GENERATOR, Upgrade.ANCHOR))
            .with(
                    new AttributeStateFacing(),
                    Attributes.ACTIVE_LIGHT,
                    Attributes.SECURITY,
                    Attributes.REDSTONE,
                    Attributes.COMPARATOR,
                    Attributes.INVENTORY
            )
            .build();

    private MCIBlockTypes() {}
}
