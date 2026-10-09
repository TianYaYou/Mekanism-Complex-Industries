package com.complexindustries.mekanism.content.pipe.network;

import com.complexindustries.mekanism.content.pipe.interfaces.IInputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.IPipeNode;
import com.complexindustries.mekanism.content.pipe.interfaces.IPowerInterface;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IndustrialPipeNetwork {

    private final UUID id = UUID.randomUUID();
    private final Set<IPipeNode> nodes = Collections.synchronizedSet(new HashSet<>());
    private final Set<IInputInterface> inputInterfaces = Collections.synchronizedSet(new HashSet<>());
    private final Set<IOutputInterface> outputInterfaces = Collections.synchronizedSet(new HashSet<>());
    private final Set<IPowerInterface> powerInterfaces = Collections.synchronizedSet(new HashSet<>());

    // Round-robin tracking per priority group
    private final Map<Integer, Integer> outputPriorityRoundRobinIndex = new ConcurrentHashMap<>();

    public IndustrialPipeNetwork() {
    }

    public UUID getId() {
        return id;
    }

    public Set<IPipeNode> getNodes() {
        return nodes;
    }

    public Set<IInputInterface> getInputInterfaces() {
        return inputInterfaces;
    }

    public Set<IOutputInterface> getOutputInterfaces() {
        return outputInterfaces;
    }

    public Set<IPowerInterface> getPowerInterfaces() {
        return powerInterfaces;
    }

    public void addNode(IPipeNode node) {
        if (nodes.add(node)) {
            node.setPipeNetwork(this);
            // Check for attachments on this node
            for (Direction dir : Direction.values()) {
                var attachment = node.getAttachment(dir);
                if (attachment != null) {
                    attachment.onAttachedToNetwork(this);
                }
            }
        }
    }

    public void removeNode(IPipeNode node) {
        if (nodes.remove(node)) {
            for (Direction dir : Direction.values()) {
                var attachment = node.getAttachment(dir);
                if (attachment != null) {
                    attachment.onDetachedFromNetwork(this);
                }
            }
            if (node.getPipeNetwork() == this) {
                node.setPipeNetwork(null);
            }
        }
    }

    public void addInputInterface(IInputInterface in) {
        inputInterfaces.add(in);
    }

    public void removeInputInterface(IInputInterface in) {
        inputInterfaces.remove(in);
    }

    public void addOutputInterface(IOutputInterface out) {
        outputInterfaces.add(out);
    }

    public void removeOutputInterface(IOutputInterface out) {
        outputInterfaces.remove(out);
    }

    public void addPowerInterface(IPowerInterface pwr) {
        powerInterfaces.add(pwr);
    }

    public void removePowerInterface(IPowerInterface pwr) {
        powerInterfaces.remove(pwr);
    }

    public void merge(IndustrialPipeNetwork other) {
        if (other == this) {
            return;
        }
        List<IPipeNode> otherNodes;
        synchronized (other.nodes) {
            otherNodes = new ArrayList<>(other.nodes);
        }
        for (IPipeNode node : otherNodes) {
            other.removeNode(node);
            addNode(node);
        }

        List<IInputInterface> otherInputs;
        synchronized (other.inputInterfaces) {
            otherInputs = new ArrayList<>(other.inputInterfaces);
        }
        for (IInputInterface in : otherInputs) {
            other.removeInputInterface(in);
            addInputInterface(in);
        }

        List<IOutputInterface> otherOutputs;
        synchronized (other.outputInterfaces) {
            otherOutputs = new ArrayList<>(other.outputInterfaces);
        }
        for (IOutputInterface out : otherOutputs) {
            other.removeOutputInterface(out);
            addOutputInterface(out);
        }

        List<IPowerInterface> otherPower;
        synchronized (other.powerInterfaces) {
            otherPower = new ArrayList<>(other.powerInterfaces);
        }
        for (IPowerInterface pwr : otherPower) {
            other.removePowerInterface(pwr);
            addPowerInterface(pwr);
        }
    }

    // =========================================================================
    //  Instant Pass-Through Routing (Items, Fluids, Chemicals)
    // =========================================================================

    public ItemStack routeItem(IInputInterface fromInput, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || (fromInput != null && !fromInput.canOperate())) {
            return stack;
        }

        List<IOutputInterface> snapshot;
        synchronized (outputInterfaces) {
            if (outputInterfaces.isEmpty()) {
                return stack;
            }
            snapshot = new ArrayList<>(outputInterfaces);
        }

        // Strict whitelist: must match output filter and be enabled by redstone
        List<IOutputInterface> matching = snapshot.stream()
                .filter(out -> out.canOperate() && out.matchesItem(stack))
                .sorted(Comparator.comparingInt(IOutputInterface::getPriority).reversed())
                .toList();

        if (matching.isEmpty()) {
            return stack; // Rejected! Blocks auto-ejection!
        }

        ItemStack remaining = stack.copy();

        // Group by priority descending
        Map<Integer, List<IOutputInterface>> byPriority = matching.stream()
                .collect(Collectors.groupingBy(IOutputInterface::getPriority, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<IOutputInterface>> entry : byPriority.entrySet()) {
            int priority = entry.getKey();
            List<IOutputInterface> group = entry.getValue();
            int offset = outputPriorityRoundRobinIndex.getOrDefault(priority, 0);
            int size = group.size();

            for (int i = 0; i < size; i++) {
                IOutputInterface out = group.get((offset + i) % size);
                ItemStack before = remaining.copy();
                remaining = out.insertItemIntoTarget(remaining, simulate);

                if (remaining.getCount() < before.getCount() && !simulate) {
                    outputPriorityRoundRobinIndex.put(priority, (offset + i + 1) % size);
                }

                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return remaining;
    }

    public FluidStack routeFluid(IInputInterface fromInput, FluidStack stack, boolean simulate) {
        if (stack.isEmpty() || (fromInput != null && !fromInput.canOperate())) {
            return stack;
        }

        List<IOutputInterface> snapshot;
        synchronized (outputInterfaces) {
            if (outputInterfaces.isEmpty()) {
                return stack;
            }
            snapshot = new ArrayList<>(outputInterfaces);
        }

        List<IOutputInterface> matching = snapshot.stream()
                .filter(out -> out.canOperate() && out.matchesFluid(stack))
                .sorted(Comparator.comparingInt(IOutputInterface::getPriority).reversed())
                .toList();

        if (matching.isEmpty()) {
            return stack;
        }

        FluidStack remaining = stack.copy();
        Map<Integer, List<IOutputInterface>> byPriority = matching.stream()
                .collect(Collectors.groupingBy(IOutputInterface::getPriority, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<IOutputInterface>> entry : byPriority.entrySet()) {
            int priority = entry.getKey();
            List<IOutputInterface> group = entry.getValue();
            int offset = outputPriorityRoundRobinIndex.getOrDefault(priority, 0);
            int size = group.size();

            for (int i = 0; i < size; i++) {
                IOutputInterface out = group.get((offset + i) % size);
                int beforeAmount = remaining.getAmount();
                remaining = out.insertFluidIntoTarget(remaining, simulate);

                if (remaining.getAmount() < beforeAmount && !simulate) {
                    outputPriorityRoundRobinIndex.put(priority, (offset + i + 1) % size);
                }

                if (remaining.isEmpty()) {
                    return FluidStack.EMPTY;
                }
            }
        }

        return remaining;
    }

    public ChemicalStack routeChemical(IInputInterface fromInput, ChemicalStack stack, boolean simulate) {
        if (stack.isEmpty() || (fromInput != null && !fromInput.canOperate())) {
            return stack;
        }

        List<IOutputInterface> snapshot;
        synchronized (outputInterfaces) {
            if (outputInterfaces.isEmpty()) {
                return stack;
            }
            snapshot = new ArrayList<>(outputInterfaces);
        }

        List<IOutputInterface> matching = snapshot.stream()
                .filter(out -> out.canOperate() && out.matchesChemical(stack))
                .sorted(Comparator.comparingInt(IOutputInterface::getPriority).reversed())
                .toList();

        if (matching.isEmpty()) {
            return stack;
        }

        ChemicalStack remaining = stack.copy();
        Map<Integer, List<IOutputInterface>> byPriority = matching.stream()
                .collect(Collectors.groupingBy(IOutputInterface::getPriority, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<IOutputInterface>> entry : byPriority.entrySet()) {
            int priority = entry.getKey();
            List<IOutputInterface> group = entry.getValue();
            int offset = outputPriorityRoundRobinIndex.getOrDefault(priority, 0);
            int size = group.size();

            for (int i = 0; i < size; i++) {
                IOutputInterface out = group.get((offset + i) % size);
                long beforeAmount = remaining.getAmount();
                remaining = out.insertChemicalIntoTarget(remaining, simulate);

                if (remaining.getAmount() < beforeAmount && !simulate) {
                    outputPriorityRoundRobinIndex.put(priority, (offset + i + 1) % size);
                }

                if (remaining.isEmpty()) {
                    return ChemicalStack.EMPTY;
                }
            }
        }

        return remaining;
    }

    // =========================================================================
    //  On-Demand Power Pass-Through (Zero Network Buffer)
    // =========================================================================

    /**
     * Extracts energy on-demand from external generators connected to power interfaces.
     */
    public int extractEnergy(int maxAmount, boolean simulate) {
        if (maxAmount <= 0) {
            return 0;
        }

        List<IPowerInterface> snapshot;
        synchronized (powerInterfaces) {
            if (powerInterfaces.isEmpty()) {
                return 0;
            }
            snapshot = new ArrayList<>(powerInterfaces);
        }

        int totalExtracted = 0;
        int needed = maxAmount;

        for (IPowerInterface pwr : snapshot) {
            if (!pwr.canOperate()) {
                continue;
            }
            int extracted = pwr.extractEnergyFromExternal(needed, simulate);
            totalExtracted += extracted;
            needed -= extracted;
            if (needed <= 0) {
                break;
            }
        }
        return totalExtracted;
    }

    private record EnergyConsumer(int priority, IEnergyStorage handler) {}

    /**
     * Ticks power dispatch every server tick.
     * Evaluates external machines connected to input/output interfaces,
     * sorts by priority, extracts strictly on-demand from power interfaces,
     * and powers the machines.
     */
    public void tickServer(ServerLevel level) {
        List<IPowerInterface> powerSnapshot;
        synchronized (powerInterfaces) {
            if (powerInterfaces.isEmpty()) {
                return;
            }
            powerSnapshot = new ArrayList<>(powerInterfaces);
        }

        List<IInputInterface> inputSnapshot;
        synchronized (inputInterfaces) {
            inputSnapshot = new ArrayList<>(inputInterfaces);
        }

        List<IOutputInterface> outputSnapshot;
        synchronized (outputInterfaces) {
            outputSnapshot = new ArrayList<>(outputInterfaces);
        }

        // Gather all consumers from Input and Output interfaces (enabled by redstone)
        List<EnergyConsumer> consumers = new ArrayList<>();

        for (IInputInterface in : inputSnapshot) {
            if (!in.canOperate()) {
                continue;
            }
            collectConsumers(level, in.getInterfacePos(), in.getAttachedFace(), in.getPriority(), consumers);
        }
        for (IOutputInterface out : outputSnapshot) {
            if (!out.canOperate()) {
                continue;
            }
            collectConsumers(level, out.getInterfacePos(), out.getAttachedFace(), out.getPriority(), consumers);
        }

        if (consumers.isEmpty()) {
            return;
        }

        // Sort consumers by priority descending
        consumers.sort(Comparator.comparingInt(EnergyConsumer::priority).reversed());

        for (EnergyConsumer consumer : consumers) {
            int needed = consumer.handler().receiveEnergy(Integer.MAX_VALUE, true);
            if (needed > 0) {
                // Simulate extraction first
                int available = extractEnergy(needed, true);
                if (available > 0) {
                    // Extract for real and insert
                    int extracted = extractEnergy(available, false);
                    consumer.handler().receiveEnergy(extracted, false);
                }
            }
        }
    }

    private void collectConsumers(ServerLevel level, BlockPos pos, Direction attachedFace, int priority, List<EnergyConsumer> list) {
        if (!WorldUtils.isBlockLoaded(level, pos)) {
            return;
        }

        if (attachedFace != null) {
            // Part form: single facing target
            BlockPos targetPos = pos.relative(attachedFace);
            if (!WorldUtils.isBlockLoaded(level, targetPos)) {
                return;
            }
            IEnergyStorage storage = WorldUtils.getCapability(level, Capabilities.EnergyStorage.BLOCK, targetPos, attachedFace.getOpposite());
            if (storage != null && storage.canReceive()) {
                list.add(new EnergyConsumer(priority, storage));
            }
        } else {
            // Block form: check all 6 adjacent sides
            for (Direction dir : Direction.values()) {
                BlockPos targetPos = pos.relative(dir);
                if (!WorldUtils.isBlockLoaded(level, targetPos)) {
                    continue;
                }
                // Don't push to other nodes of our network
                if (WorldUtils.getTileEntity(level, targetPos) instanceof IPipeNode) {
                    continue;
                }
                IEnergyStorage storage = WorldUtils.getCapability(level, Capabilities.EnergyStorage.BLOCK, targetPos, dir.getOpposite());
                if (storage != null && storage.canReceive()) {
                    list.add(new EnergyConsumer(priority, storage));
                }
            }
        }
    }
}
