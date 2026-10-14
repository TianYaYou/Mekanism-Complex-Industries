package com.complexindustries.mekanism.content.precipitator;

import com.complexindustries.mekanism.registration.MCIContainerTypes;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.api.Upgrade;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
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
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockSupercriticalOxidationPrecipitator extends Block
        implements IHasTileEntity<TileEntitySupercriticalOxidationPrecipitator>, ITypeBlock, IHasDescription {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    // Non-full-block custom VoxelShape:
    // 1) Heavy base pedestal (1..15, 0..3, 1..15)
    // 2) Supercritical reaction vessel & side fluid/aerosol columns (2..14, 3..14, 2..14)
    // 3) Top high-pressure manifold cap (4..14, 14..16, 4..12)
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 3.0, 15.0),
            Block.box(2.0, 3.0, 2.0, 14.0, 14.0, 14.0),
            Block.box(4.0, 14.0, 4.0, 12.0, 16.0, 12.0)
    );

    private final BlockTypeTile<TileEntitySupercriticalOxidationPrecipitator> type;

    public BlockSupercriticalOxidationPrecipitator(BlockBehaviour.Properties properties) {
        super(properties);
        this.type = BlockTypeTile.BlockTileBuilder.createBlock(
                () -> MCITileEntityTypes.SUPERCRITICAL_OXIDATION_PRECIPITATOR,
                () -> "description.mekanism_complex_industries.supercritical_oxidation_precipitator"
        )
        .withGui(() -> MCIContainerTypes.SUPERCRITICAL_OXIDATION_PRECIPITATOR)
        .withEnergyConfig(() -> TileEntitySupercriticalOxidationPrecipitator.BASE_ENERGY_PER_TICK, () -> 160_000L)
        .with(AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.CHEMICAL, TransmissionType.ENERGY))
        .with(Attributes.SECURITY, Attributes.INVENTORY, Attributes.REDSTONE, new AttributeStateFacing())
        .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING)
        .build();

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 0.8F;
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
    public TileEntityTypeRegistryObject<TileEntitySupercriticalOxidationPrecipitator> getTileType() {
        return MCITileEntityTypes.SUPERCRITICAL_OXIDATION_PRECIPITATOR;
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world,
            @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
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
    protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull BlockHitResult hit) {
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

    @NotNull
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @NotNull
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntitySupercriticalOxidationPrecipitator tile) {
            tile.onNeighborChange(block, fromPos);
        }
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityMekanism tile) {
                tile.blockRemoved();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
