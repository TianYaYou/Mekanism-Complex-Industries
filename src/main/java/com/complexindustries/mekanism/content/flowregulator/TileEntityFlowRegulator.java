package com.complexindustries.mekanism.content.flowregulator;

import com.complexindustries.mekanism.registration.MCIBlocks;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFlowRegulator extends TileEntityMekanism {

    public static final long DEFAULT_BUFFER = 10_000L;
    public static final long DEFAULT_FLOW_RATE = 1_000L; // 1,000 mB/s

    public IChemicalTank bufferTank;
    private long flowRate = DEFAULT_FLOW_RATE;
    private long lastTransferredRate = 0L;
    private long transferredThisSecond = 0L;
    private int tickCounter = 0;
    private long flowAccumulator = 0L;
    private long remainingBudget = 0L;
    private DyeColor ringColor = DyeColor.WHITE;

    public TileEntityFlowRegulator(BlockPos pos, BlockState state) {
        super(MCIBlocks.FLOW_REGULATOR, pos, state);
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper helper = ChemicalTankHelper.forSide(this::getDirection);
        helper.addTank(bufferTank = new BasicChemicalTank(DEFAULT_BUFFER,
                ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(),
                ConstantPredicates.alwaysTrue(), null, listener, null) {
            @Override
            public long getCapacity() {
                return Math.max(DEFAULT_BUFFER, flowRate);
            }
        }, RelativeSide.FRONT, RelativeSide.BACK);
        return helper.build();
    }

    @Override
    public ChemicalStack insertChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        if (side != null && (side == getDirection() || !canFunction())) {
            // Cannot insert into output face; cannot insert if shut off by redstone
            return stack;
        }
        return super.insertChemical(stack, side, action);
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, @Nullable Direction side, Action action) {
        if (side != null && (side == getDirection() || !canFunction())) {
            return stack;
        }
        return super.insertChemical(tank, stack, side, action);
    }

    @Override
    public ChemicalStack extractChemical(long amount, @Nullable Direction side, Action action) {
        if (side != null && (side == getDirection().getOpposite() || !canFunction())) {
            // Cannot extract from input face; cannot extract if shut off by redstone
            return ChemicalStack.EMPTY;
        }
        long allowed = side == null ? amount : Math.min(amount, remainingBudget);
        if (allowed <= 0) {
            return ChemicalStack.EMPTY;
        }
        ChemicalStack extracted = super.extractChemical(allowed, side, action);
        if (action.execute() && side != null) {
            remainingBudget -= extracted.getAmount();
            transferredThisSecond += extracted.getAmount();
        }
        return extracted;
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, @Nullable Direction side, Action action) {
        if (side != null && (side == getDirection().getOpposite() || !canFunction())) {
            return ChemicalStack.EMPTY;
        }
        long allowed = side == null ? amount : Math.min(amount, remainingBudget);
        if (allowed <= 0) {
            return ChemicalStack.EMPTY;
        }
        ChemicalStack extracted = super.extractChemical(tank, allowed, side, action);
        if (action.execute() && side != null) {
            remainingBudget -= extracted.getAmount();
            transferredThisSecond += extracted.getAmount();
        }
        return extracted;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();

        // 1. One-second transfer rate tracking
        tickCounter++;
        if (tickCounter >= 20) {
            lastTransferredRate = transferredThisSecond;
            transferredThisSecond = 0L;
            tickCounter = 0;
        }

        // 2. Redstone shut-off or zero flow limit
        if (!canFunction() || flowRate <= 0) {
            remainingBudget = 0L;
            flowAccumulator = 0L;
            return sendUpdatePacket;
        }

        // 3. Exact per-tick token budget calculation
        flowAccumulator += flowRate;
        remainingBudget = flowAccumulator / 20L;
        flowAccumulator %= 20L;

        // 4. Auto-ejection to output port
        if (remainingBudget > 0 && !bufferTank.isEmpty()) {
            Direction front = getDirection();
            BlockPos targetPos = worldPosition.relative(front);
            IChemicalHandler target = level.getCapability(Capabilities.CHEMICAL.block(), targetPos, front.getOpposite());
            if (target != null) {
                long toEjectAmount = Math.min(remainingBudget, bufferTank.getStored());
                if (toEjectAmount > 0) {
                    ChemicalStack toSend = bufferTank.getStack().copyWithAmount(toEjectAmount);
                    ChemicalStack remainder = target.insertChemical(toSend, Action.EXECUTE);
                    long sent = toEjectAmount - remainder.getAmount();
                    if (sent > 0) {
                        bufferTank.shrinkStack(sent, Action.EXECUTE);
                        remainingBudget -= sent;
                        transferredThisSecond += sent;
                    }
                }
            }
        }

        return sendUpdatePacket;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableLong.create(this::getFlowRate, this::setFlowRate));
        container.track(SyncableLong.create(this::getLastTransferredRate, val -> this.lastTransferredRate = val));
    }

    public long getFlowRate() {
        return flowRate;
    }

    public void setFlowRate(long rate) {
        this.flowRate = Math.max(0, rate);
        markForSave();
    }

    public long getLastTransferredRate() {
        return lastTransferredRate;
    }

    public DyeColor getRingColor() {
        return ringColor;
    }

    public void setRingColor(DyeColor color) {
        if (this.ringColor != color) {
            this.ringColor = color;
            markForSave();
            sendUpdatePacket();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putLong("flowRate", flowRate);
        tag.putString("ringColor", ringColor.getName());
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("flowRate")) {
            flowRate = tag.getLong("flowRate");
        }
        if (tag.contains("ringColor")) {
            ringColor = DyeColor.byName(tag.getString("ringColor"), DyeColor.WHITE);
        }
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag tag = super.getReducedUpdateTag(provider);
        tag.putInt("ringColor", ringColor.getId());
        tag.putLong("flowRate", flowRate);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("ringColor")) {
            this.ringColor = DyeColor.byId(tag.getInt("ringColor"));
        }
        if (tag.contains("flowRate")) {
            this.flowRate = tag.getLong("flowRate");
        }
    }
}
