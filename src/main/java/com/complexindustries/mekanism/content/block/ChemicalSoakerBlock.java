package com.complexindustries.mekanism.content.block;

import com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoaker;
import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import com.complexindustries.mekanism.registration.MCIItems;
import com.complexindustries.mekanism.registration.MCITileEntityTypes;
import mekanism.api.Upgrade;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.VoxelShapeUtils;
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

public class ChemicalSoakerBlock extends Block implements IHasTileEntity<TileEntityChemicalSoaker>, ITypeBlock, IHasDescription {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public static final VoxelShape[] SHAPES = new VoxelShape[4];
    static {
        // Non-full immersion vat shape: base 0..3, recessed vat 1..15 up to height 14 (open top)
        VoxelShape baseShape = Shapes.or(
                Block.box(0, 0, 0, 16, 3, 16),
                Block.box(1, 3, 1, 15, 14, 15)
        );
        VoxelShapeUtils.setShape(baseShape, SHAPES);
    }

    private final BlockTypeTile<TileEntityChemicalSoaker> type;

    public ChemicalSoakerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.type = BlockTypeTile.BlockTileBuilder.createBlock(
                () -> MCITileEntityTypes.CHEMICAL_SOAKER,
                () -> "description.mekanism_complex_industries.chemical_soaker"
        )
        .withGui(() -> MCIContainerTypes.CHEMICAL_SOAKER)
        .withEnergyConfig(() -> 200L, () -> 100_000L)
        .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
        .with(Attributes.SECURITY, Attributes.INVENTORY)
        .withCustomShape(SHAPES)
        .with(new mekanism.common.block.attribute.AttributeStateFacing())
        .with(new mekanism.common.block.attribute.AttributeUpgradeable(() -> new BlockRegistryObject<>(MCIBlocks.BASIC_CHEMICAL_SOAKING_FACTORY, MCIItems.BASIC_CHEMICAL_SOAKING_FACTORY)))
        .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING)
        .build();
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Direction facing = state.getValue(FACING);
        int index = facing.ordinal() - 2;
        if (index >= 0 && index < SHAPES.length) {
            return SHAPES[index];
        }
        return Shapes.block();
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
    public TileEntityTypeRegistryObject<TileEntityChemicalSoaker> getTileType() {
        return MCITileEntityTypes.CHEMICAL_SOAKER;
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
        if (blockEntity instanceof TileEntityChemicalSoaker tile) {
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
