package com.complexindustries.mekanism.content.pipe.network;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.pipe.interfaces.IPipeNode;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = MCIConstants.MODID)
public final class PipeNetworkManager {

    private static final Map<ResourceKey<Level>, Set<IndustrialPipeNetwork>> ACTIVE_NETWORKS = new ConcurrentHashMap<>();

    private PipeNetworkManager() {}

    public static Set<IndustrialPipeNetwork> getNetworksForLevel(Level level) {
        return ACTIVE_NETWORKS.computeIfAbsent(level.dimension(), k -> Collections.synchronizedSet(new HashSet<>()));
    }

    private static final ThreadLocal<Set<IPipeNode>> INITIALIZING_NODES = ThreadLocal.withInitial(HashSet::new);

    public static synchronized void onNodeAdded(Level level, BlockPos pos, IPipeNode node) {
        if (level.isClientSide()) {
            return;
        }

        if (node.getPipeNetwork() != null) {
            return;
        }

        Set<IPipeNode> initializing = INITIALIZING_NODES.get();
        if (!initializing.add(node)) {
            return;
        }

        try {
            Set<IndustrialPipeNetwork> adjacentNetworks = new HashSet<>();
            List<IPipeNode> adjacentNodes = new ArrayList<>();

            for (Direction dir : Direction.values()) {
                if (!node.canConnectPipe(dir)) {
                    continue;
                }
                BlockPos neighborPos = pos.relative(dir);
                if (!WorldUtils.isBlockLoaded(level, neighborPos)) {
                    continue;
                }
                BlockEntity be = WorldUtils.getTileEntity(level, neighborPos);
                if (be instanceof IPipeNode neighborNode && neighborNode.canConnectPipe(dir.getOpposite())) {
                    adjacentNodes.add(neighborNode);
                    IndustrialPipeNetwork net = neighborNode.getPipeNetwork();
                    if (net != null) {
                        adjacentNetworks.add(net);
                    }
                }
            }

            Set<IndustrialPipeNetwork> levelNetworks = getNetworksForLevel(level);

            if (adjacentNetworks.isEmpty()) {
                IndustrialPipeNetwork newNet = new IndustrialPipeNetwork();
                newNet.addNode(node);
                for (IPipeNode neighbor : adjacentNodes) {
                    if (neighbor.getPipeNetwork() == null) {
                        newNet.addNode(neighbor);
                    }
                }
                levelNetworks.add(newNet);
            } else {
                Iterator<IndustrialPipeNetwork> it = adjacentNetworks.iterator();
                IndustrialPipeNetwork primary = it.next();
                while (it.hasNext()) {
                    IndustrialPipeNetwork other = it.next();
                    primary.merge(other);
                    levelNetworks.remove(other);
                }
                primary.addNode(node);
                for (IPipeNode neighbor : adjacentNodes) {
                    if (neighbor.getPipeNetwork() == null) {
                        primary.addNode(neighbor);
                    }
                }
            }
        } finally {
            initializing.remove(node);
        }
    }

    /**
     * Called when a pipe block is physically destroyed or removed from the world.
     * Partitions the graph and creates new subnetworks if disconnected.
     */
    public static synchronized void onNodeRemoved(Level level, BlockPos pos, IPipeNode node) {
        if (level.isClientSide()) {
            return;
        }

        IndustrialPipeNetwork net = node.getPipeNetwork();
        if (net == null) {
            return;
        }

        net.removeNode(node);
        Set<IndustrialPipeNetwork> levelNetworks = getNetworksForLevel(level);

        if (net.getNodes().isEmpty()) {
            levelNetworks.remove(net);
            return;
        }

        // Collect remaining adjacent nodes that were connected to the removed node (loaded chunks only)
        List<IPipeNode> remainingAdjacent = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            if (!WorldUtils.isBlockLoaded(level, neighborPos)) {
                continue;
            }
            BlockEntity be = WorldUtils.getTileEntity(level, neighborPos);
            if (be instanceof IPipeNode neighborNode && neighborNode.getPipeNetwork() == net) {
                remainingAdjacent.add(neighborNode);
            }
        }

        if (remainingAdjacent.size() <= 1) {
            return; // Removing an endpoint or isolated leaf cannot partition the graph
        }

        // Check if graph split into multiple connected components
        List<Set<IPipeNode>> components = new ArrayList<>();
        Set<IPipeNode> visitedAll = new HashSet<>();

        for (IPipeNode startNode : remainingAdjacent) {
            if (visitedAll.contains(startNode)) {
                continue;
            }

            Set<IPipeNode> component = new HashSet<>();
            Queue<IPipeNode> queue = new ArrayDeque<>();
            queue.add(startNode);
            component.add(startNode);
            visitedAll.add(startNode);

            while (!queue.isEmpty()) {
                IPipeNode current = queue.poll();
                BlockPos cPos = current.getNodePos();

                for (Direction dir : Direction.values()) {
                    if (!current.canConnectPipe(dir)) {
                        continue;
                    }
                    BlockPos nextPos = cPos.relative(dir);
                    if (!WorldUtils.isBlockLoaded(level, nextPos)) {
                        continue;
                    }
                    BlockEntity be = WorldUtils.getTileEntity(level, nextPos);
                    if (be instanceof IPipeNode nextNode && nextNode.getPipeNetwork() == net && nextNode.canConnectPipe(dir.getOpposite())) {
                        if (component.add(nextNode)) {
                            visitedAll.add(nextNode);
                            queue.add(nextNode);
                        }
                    }
                }
            }
            components.add(component);
        }

        // If partitioned into 2 or more components, leave first in 'net', split the rest into new networks
        if (components.size() > 1) {
            for (int i = 1; i < components.size(); i++) {
                Set<IPipeNode> compNodes = components.get(i);
                IndustrialPipeNetwork newNet = new IndustrialPipeNetwork();
                for (IPipeNode n : compNodes) {
                    net.removeNode(n);
                    newNet.addNode(n);
                }
                levelNetworks.add(newNet);
            }
        }
    }

    /**
     * Called when a chunk is unloaded. Simply detaches the node from active memory without
     * traversing neighbors or loading adjacent chunks.
     */
    public static synchronized void onNodeUnloaded(Level level, BlockPos pos, IPipeNode node) {
        if (level.isClientSide()) {
            return;
        }

        IndustrialPipeNetwork net = node.getPipeNetwork();
        if (net != null) {
            net.removeNode(node);
            if (net.getNodes().isEmpty()) {
                Set<IndustrialPipeNetwork> levelNetworks = ACTIVE_NETWORKS.get(level.dimension());
                if (levelNetworks != null) {
                    levelNetworks.remove(net);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            Set<IndustrialPipeNetwork> networks = ACTIVE_NETWORKS.get(serverLevel.dimension());
            if (networks != null && !networks.isEmpty()) {
                List<IndustrialPipeNetwork> copy;
                synchronized (networks) {
                    copy = new ArrayList<>(networks);
                }
                for (IndustrialPipeNetwork net : copy) {
                    net.tickServer(serverLevel);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ACTIVE_NETWORKS.remove(serverLevel.dimension());
        }
    }
}
