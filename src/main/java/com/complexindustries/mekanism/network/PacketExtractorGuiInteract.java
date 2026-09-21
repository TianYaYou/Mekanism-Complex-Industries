package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.content.extractor.TileEntityCrudeOilExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketExtractorGuiInteract {

    public enum Action {
        START,
        STOP,
        RESET,
        SET_RADIUS,
        SET_MIN_Y,
        SET_MAX_Y
    }

    private final Action action;
    private final BlockPos pos;
    private final int value;

    public PacketExtractorGuiInteract(Action action, BlockPos pos, int value) {
        this.action = action;
        this.pos = pos;
        this.value = value;
    }

    public PacketExtractorGuiInteract(Action action, BlockPos pos) {
        this(action, pos, 0);
    }

    public static void encode(PacketExtractorGuiInteract msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.value);
    }

    public static PacketExtractorGuiInteract decode(FriendlyByteBuf buf) {
        Action action = buf.readEnum(Action.class);
        BlockPos pos = buf.readBlockPos();
        int value = buf.readVarInt();
        return new PacketExtractorGuiInteract(action, pos, value);
    }

    public static void handle(PacketExtractorGuiInteract msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level().isLoaded(msg.pos)) {
                if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) <= 64.0) {
                    BlockEntity tile = player.level().getBlockEntity(msg.pos);
                    if (tile instanceof TileEntityCrudeOilExtractor extractor) {
                        switch (msg.action) {
                            case START -> extractor.start();
                            case STOP -> extractor.stop();
                            case RESET -> extractor.reset();
                            case SET_RADIUS -> extractor.setRadius(msg.value);
                            case SET_MIN_Y -> extractor.setMinY(msg.value);
                            case SET_MAX_Y -> extractor.setMaxY(msg.value);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
