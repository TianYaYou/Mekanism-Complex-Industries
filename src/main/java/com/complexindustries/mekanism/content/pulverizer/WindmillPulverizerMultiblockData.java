package com.complexindustries.mekanism.content.pulverizer;

import com.complexindustries.mekanism.registration.MCIBlocks;
import com.complexindustries.mekanism.registration.MCIChemicals;
import com.complexindustries.mekanism.registration.MCIItems;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class WindmillPulverizerMultiblockData extends MultiblockData implements IValveHandler {

    public static final int GROUP_COUNT = 6;
    public static final int BATCH_ORE_COUNT = 32;
    public static final int MULTIPLIER = 6;
    public static final int BATCH_OUTPUT_COUNT = BATCH_ORE_COUNT * MULTIPLIER; // 192 powders
    public static final long NITROGEN_PER_BATCH = 1_000L; // 1,000 mB
    public static final long ENERGY_PER_TICK_PER_GROUP = 500L; // 500 J/t (~200 FE/t)
    public static final int BASE_TICKS = 600; // 30 seconds (600 ticks)

    @ContainerSync
    public mekanism.api.chemical.IChemicalTank chemicalTank;

    @ContainerSync
    public VariableCapacityEnergyContainer energyContainer;

    @ContainerSync
    public int bladesCount = 0;

    @ContainerSync
    public int coilsCount = 0;

    @ContainerSync
    public int rotorHeight = 0;

    @ContainerSync
    public float speedMultiplier = 1.0f;

    @ContainerSync
    public long lastEnergyUsage = 0;

    // 6 Parallel Groups Individual Synced Fields
    @ContainerSync
    public int groupProgress0 = 0, groupProgress1 = 0, groupProgress2 = 0, groupProgress3 = 0, groupProgress4 = 0, groupProgress5 = 0;

    @ContainerSync
    public int groupMaxProgress0 = BASE_TICKS, groupMaxProgress1 = BASE_TICKS, groupMaxProgress2 = BASE_TICKS,
               groupMaxProgress3 = BASE_TICKS, groupMaxProgress4 = BASE_TICKS, groupMaxProgress5 = BASE_TICKS;

    @ContainerSync
    public boolean groupActive0 = false, groupActive1 = false, groupActive2 = false,
                   groupActive3 = false, groupActive4 = false, groupActive5 = false;

    public final ItemStack[] groupProcessingType = new ItemStack[GROUP_COUNT];

    // Input slots (6 slots: index 0 to 5)
    public final List<BasicInventorySlot> inputSlots = new ArrayList<>();

    // Output slots (6 slots: index 0 to 5)
    public final List<OutputInventorySlot> outputSlots = new ArrayList<>();

    public WindmillPulverizerMultiblockData(TileEntityMultiblock<?> tile) {
        super(tile);
        IContentsListener saveAndComparator = createSaveAndComparator();

        chemicalTank = VariableCapacityChemicalTank.input(
                this,
                () -> 100_000L,
                stack -> stack.is(MCIChemicals.NITROGEN),
                saveAndComparator
        );
        chemicalTanks.add(chemicalTank);

        energyContainer = VariableCapacityEnergyContainer.input(
                () -> 10_000_000L,
                this
        );
        energyContainers.add(energyContainer);

        // Initialize 6 input slots and 6 output slots with exact pixel positions
        for (int i = 0; i < GROUP_COUNT; i++) {
            int slotX = 30 + i * 20;

            BasicInventorySlot inSlot = BasicInventorySlot.at(
                    stack -> getOutputPowder(stack) != null,
                    saveAndComparator,
                    slotX, 17
            );
            inputSlots.add(inSlot);
            inventorySlots.add(inSlot);

            OutputInventorySlot outSlot = OutputInventorySlot.at(saveAndComparator, slotX, 57);
            outputSlots.add(outSlot);
            inventorySlots.add(outSlot);

            groupProcessingType[i] = ItemStack.EMPTY;
            setGroupMaxProgress(i, BASE_TICKS);
        }
    }

    public int getGroupProgress(int i) {
        return switch (i) {
            case 0 -> groupProgress0;
            case 1 -> groupProgress1;
            case 2 -> groupProgress2;
            case 3 -> groupProgress3;
            case 4 -> groupProgress4;
            case 5 -> groupProgress5;
            default -> 0;
        };
    }

    public void setGroupProgress(int i, int val) {
        switch (i) {
            case 0 -> groupProgress0 = val;
            case 1 -> groupProgress1 = val;
            case 2 -> groupProgress2 = val;
            case 3 -> groupProgress3 = val;
            case 4 -> groupProgress4 = val;
            case 5 -> groupProgress5 = val;
        }
    }

    public int getGroupMaxProgress(int i) {
        return switch (i) {
            case 0 -> groupMaxProgress0;
            case 1 -> groupMaxProgress1;
            case 2 -> groupMaxProgress2;
            case 3 -> groupMaxProgress3;
            case 4 -> groupMaxProgress4;
            case 5 -> groupMaxProgress5;
            default -> BASE_TICKS;
        };
    }

    public void setGroupMaxProgress(int i, int val) {
        switch (i) {
            case 0 -> groupMaxProgress0 = val;
            case 1 -> groupMaxProgress1 = val;
            case 2 -> groupMaxProgress2 = val;
            case 3 -> groupMaxProgress3 = val;
            case 4 -> groupMaxProgress4 = val;
            case 5 -> groupMaxProgress5 = val;
        }
    }

    public boolean isGroupActive(int i) {
        return switch (i) {
            case 0 -> groupActive0;
            case 1 -> groupActive1;
            case 2 -> groupActive2;
            case 3 -> groupActive3;
            case 4 -> groupActive4;
            case 5 -> groupActive5;
            default -> false;
        };
    }

    public void setGroupActive(int i, boolean val) {
        switch (i) {
            case 0 -> groupActive0 = val;
            case 1 -> groupActive1 = val;
            case 2 -> groupActive2 = val;
            case 3 -> groupActive3 = val;
            case 4 -> groupActive4 = val;
            case 5 -> groupActive5 = val;
        }
    }

    public double getScaledGroupProgress(int i) {
        int max = Math.max(1, getGroupMaxProgress(i));
        return (double) getGroupProgress(i) / max;
    }

    public static Item getOutputPowder(ItemStack input) {
        if (input.isEmpty()) {
            return null;
        }
        Item item = input.getItem();
        if (item == Items.RAW_IRON) {
            return MCIItems.CLOUDY_OXIDIZED_IRON_POWDER.get();
        } else if (item == Items.RAW_GOLD) {
            return MCIItems.CLOUDY_OXIDIZED_GOLD_POWDER.get();
        } else if (item == Items.RAW_COPPER) {
            return MCIItems.CLOUDY_OXIDIZED_COPPER_POWDER.get();
        }

        ResourceLocation loc = BuiltInRegistries.ITEM.getKey(item);
        String path = loc.getPath();
        if (path.contains("raw_osmium") || path.equals("osmium_ore")) {
            return MCIItems.CLOUDY_OXIDIZED_OSMIUM_POWDER.get();
        } else if (path.contains("raw_tin") || path.equals("tin_ore")) {
            return MCIItems.CLOUDY_OXIDIZED_TIN_POWDER.get();
        } else if (path.contains("raw_lead") || path.equals("lead_ore")) {
            return MCIItems.CLOUDY_OXIDIZED_LEAD_POWDER.get();
        } else if (path.contains("raw_uranium") || path.equals("uranium_ore")) {
            return MCIItems.CLOUDY_OXIDIZED_URANIUM_POWDER.get();
        }

        // Tag fallbacks
        if (input.is(net.minecraft.tags.ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/iron")))) {
            return MCIItems.CLOUDY_OXIDIZED_IRON_POWDER.get();
        } else if (input.is(net.minecraft.tags.ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/gold")))) {
            return MCIItems.CLOUDY_OXIDIZED_GOLD_POWDER.get();
        } else if (input.is(net.minecraft.tags.ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/copper")))) {
            return MCIItems.CLOUDY_OXIDIZED_COPPER_POWDER.get();
        } else if (input.is(net.minecraft.tags.ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/osmium")))) {
            return MCIItems.CLOUDY_OXIDIZED_OSMIUM_POWDER.get();
        } else if (input.is(net.minecraft.tags.ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/tin")))) {
            return MCIItems.CLOUDY_OXIDIZED_TIN_POWDER.get();
        } else if (input.is(net.minecraft.tags.ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/lead")))) {
            return MCIItems.CLOUDY_OXIDIZED_LEAD_POWDER.get();
        } else if (input.is(net.minecraft.tags.ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/uranium")))) {
            return MCIItems.CLOUDY_OXIDIZED_URANIUM_POWDER.get();
        }

        return null;
    }

    public void updateTurbineStats(int blades, int coils, int rotorH) {
        this.bladesCount = blades;
        this.coilsCount = coils;
        this.rotorHeight = rotorH;
        this.speedMultiplier = 1.0f + (blades * 0.05f) + (coils * 0.10f);
    }

    public int getCalculatedProcessingTicks() {
        return Math.max(100, Math.round(BASE_TICKS / Math.max(1.0f, speedMultiplier)));
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);

        if (world.isClientSide) {
            return needsPacket;
        }

        long totalEnergyThisTick = 0;

        // 1. Check if any idle group can start a new batch
        int targetTicks = getCalculatedProcessingTicks();
        for (int i = 0; i < GROUP_COUNT; i++) {
            if (!isGroupActive(i)) {
                BasicInventorySlot inSlot = inputSlots.get(i);
                if (!inSlot.isEmpty() && inSlot.getCount() >= BATCH_ORE_COUNT) {
                    Item powder = getOutputPowder(inSlot.getStack());
                    if (powder != null && chemicalTank.getStored() >= NITROGEN_PER_BATCH) {
                        chemicalTank.extract(NITROGEN_PER_BATCH, Action.EXECUTE, AutomationType.INTERNAL);
                        ItemStack rawStack = inSlot.extractItem(BATCH_ORE_COUNT, Action.EXECUTE, AutomationType.INTERNAL);
                        groupProcessingType[i] = rawStack.copy();
                        setGroupActive(i, true);
                        setGroupProgress(i, 0);
                        setGroupMaxProgress(i, targetTicks);
                        needsPacket = true;
                    }
                }
            }
        }

        // 2. Process active groups
        for (int i = 0; i < GROUP_COUNT; i++) {
            if (isGroupActive(i)) {
                if (energyContainer.getEnergy() >= ENERGY_PER_TICK_PER_GROUP) {
                    energyContainer.extract(ENERGY_PER_TICK_PER_GROUP, Action.EXECUTE, AutomationType.INTERNAL);
                    totalEnergyThisTick += ENERGY_PER_TICK_PER_GROUP;
                    int curProg = getGroupProgress(i) + 1;
                    setGroupProgress(i, curProg);

                    if (curProg >= getGroupMaxProgress(i)) {
                        Item powder = getOutputPowder(groupProcessingType[i]);
                        if (powder != null) {
                            ItemStack toDistribute = new ItemStack(powder, BATCH_OUTPUT_COUNT);
                            if (canFitOutput(toDistribute)) {
                                distributeOutput(toDistribute);
                                setGroupActive(i, false);
                                setGroupProgress(i, 0);
                                groupProcessingType[i] = ItemStack.EMPTY;
                                needsPacket = true;
                            }
                        } else {
                            setGroupActive(i, false);
                            setGroupProgress(i, 0);
                            groupProcessingType[i] = ItemStack.EMPTY;
                            needsPacket = true;
                        }
                    }
                }
            }
        }

        this.lastEnergyUsage = totalEnergyThisTick;

        // 3. Eject items from output ports to adjacent inventories
        ejectOutputs(world);

        return needsPacket;
    }

    private boolean canFitOutput(ItemStack toInsert) {
        ItemStack remaining = toInsert.copy();
        for (OutputInventorySlot outSlot : outputSlots) {
            if (remaining.isEmpty()) {
                return true;
            }
            remaining = outSlot.insertItem(remaining, Action.SIMULATE, AutomationType.INTERNAL);
        }
        return remaining.isEmpty();
    }

    private void distributeOutput(ItemStack toInsert) {
        ItemStack remaining = toInsert.copy();
        for (OutputInventorySlot outSlot : outputSlots) {
            if (remaining.isEmpty()) {
                break;
            }
            remaining = outSlot.insertItem(remaining, Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    private void ejectOutputs(Level world) {
        if (valves.isEmpty()) {
            return;
        }
        for (ValveData valve : valves) {
            BlockState state = world.getBlockState(valve.location);
            if (!state.is(MCIBlocks.WINDMILL_OUTPUT_PORT.get())) {
                continue;
            }
            BlockEntity target = WorldUtils.getTileEntity(world, valve.location.relative(valve.side));
            if (target != null && target.getLevel() != null) {
                IItemHandler handler = target.getLevel().getCapability(
                        Capabilities.ItemHandler.BLOCK,
                        valve.location.relative(valve.side),
                        valve.side.getOpposite()
                );
                if (handler != null) {
                    for (OutputInventorySlot outSlot : outputSlots) {
                        if (!outSlot.isEmpty()) {
                            ItemStack toSend = outSlot.getStack().copy();
                            toSend.setCount(Math.min(toSend.getCount(), 64));
                            ItemStack remainder = ItemHandlerHelper.insertItem(handler, toSend, false);
                            int sent = toSend.getCount() - remainder.getCount();
                            if (sent > 0) {
                                outSlot.extractItem(sent, Action.EXECUTE, AutomationType.INTERNAL);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
    }

    @Override
    public void remove(Level world, Structure structure) {
        if (inventoryID != null) {
            markDirty();
            MCIWindmillPulverizerMultiblock.PULVERIZER_MANAGER.handleDirtyMultiblock(this);
        }
        super.remove(world, structure);
    }

    @Override
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        bladesCount = tag.getInt("blades");
        coilsCount = tag.getInt("coils");
        rotorHeight = tag.getInt("rotorH");
        speedMultiplier = tag.getFloat("speedMult");
        lastEnergyUsage = tag.getLong("lastEU");

        for (int i = 0; i < GROUP_COUNT; i++) {
            setGroupProgress(i, tag.getInt("prog_" + i));
            setGroupMaxProgress(i, tag.getInt("maxP_" + i));
            setGroupActive(i, tag.getBoolean("act_" + i));
            if (tag.contains("item_" + i)) {
                groupProcessingType[i] = ItemStack.parseOptional(provider, tag.getCompound("item_" + i));
            } else {
                groupProcessingType[i] = ItemStack.EMPTY;
            }
        }
        readValves(tag);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        tag.putInt("blades", bladesCount);
        tag.putInt("coils", coilsCount);
        tag.putInt("rotorH", rotorHeight);
        tag.putFloat("speedMult", speedMultiplier);
        tag.putLong("lastEU", lastEnergyUsage);

        for (int i = 0; i < GROUP_COUNT; i++) {
            tag.putInt("prog_" + i, getGroupProgress(i));
            tag.putInt("maxP_" + i, getGroupMaxProgress(i));
            tag.putBoolean("act_" + i, isGroupActive(i));
            if (!groupProcessingType[i].isEmpty()) {
                tag.put("item_" + i, groupProcessingType[i].saveOptional(provider));
            }
        }
        writeValves(tag);
    }
}
