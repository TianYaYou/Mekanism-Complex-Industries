package com.complexindustries.mekanism.content.block;

import java.util.function.Supplier;
import com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.api.Upgrade;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChemicalSoakingFactoryBlock extends Block implements IHasTileEntity<TileEntityChemicalSoakingFactory>, ITypeBlock, IHasDescription {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public final FactoryTier tier;
    private final BlockTypeTile<TileEntityChemicalSoakingFactory> type;

    public ChemicalSoakingFactoryBlock(FactoryTier tier, @Nullable Supplier<BlockRegistryObject<?, ?>> nextTier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
        long maxEnergy = (tier.processes + 2) * 40_000L;
        mekanism.common.block.attribute.AttributeUpgradeable upgradeable = nextTier != null
                ? new mekanism.common.block.attribute.AttributeUpgradeable(nextTier)
                : null;
        mekanism.common.block.attribute.Attribute[] attributes = upgradeable != null
                ? new mekanism.common.block.attribute.Attribute[]{new AttributeTier<>(tier), upgradeable, new mekanism.common.block.attribute.AttributeStateFacing()}
                : new mekanism.common.block.attribute.Attribute[]{new AttributeTier<>(tier), new mekanism.common.block.attribute.AttributeStateFacing()};
        this.type = BlockTypeTile.BlockTileBuilder.createBlock(
                () -> MCITileEntityTypes.getSoakingFactoryTile(tier),
                () -> "description.mekanism_complex_industries.chemical_soaking_factory"
        )
        .with(attributes)
        .withGui(() -> MCIContainerTypes.CHEMICAL_SOAKING_FACTORY)
        .withEnergyConfig(() -> 200L, () -> maxEnergy)
        .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
        .with(Attributes.SECURITY, Attributes.INVENTORY)
        .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING)
        .build();
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @NotNull
    @Override
    public ILangEntry getDescription() {
        return this.type.getDescription();
    }

    @Override
    public BlockType getType() {
        return this.type;
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityChemicalSoakingFactory> getTileType() {
        return MCITileEntityTypes.getSoakingFactoryTile(this.tier);
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
            @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        } else if (world.isClientSide) {
            if (!Attribute.has(this, AttributeGui.class) && MekanismUtils.canUseAsWrench(stack)) {
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return tile.tryWrench(state, player, stack).getInteractionResult();
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        } else if (world.isClientSide) {
            return Attribute.has(this, AttributeGui.class) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return tile.openGui(player);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityChemicalSoakingFactory tile) {
            tile.onNeighborChange(block, fromPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityMekanism tile) {
                tile.blockRemoved();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
