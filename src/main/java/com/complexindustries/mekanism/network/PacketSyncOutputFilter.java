package com.complexindustries.mekanism.network;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.content.pipe.container.ContainerOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record PacketSyncOutputFilter(List<OutputInterfaceFilter.FilterEntry> entries) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketSyncOutputFilter> TYPE =
            new CustomPacketPayload.Type<>(MCIConstants.rl("sync_output_filter"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncOutputFilter> STREAM_CODEC = StreamCodec.ofMember(
            PacketSyncOutputFilter::write,
            PacketSyncOutputFilter::decode
    );

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeByte(entries.size());
        for (OutputInterfaceFilter.FilterEntry entry : entries) {
            buf.writeBoolean(!entry.isEmpty());
            if (!entry.isEmpty()) {
                buf.writeByte(entry.getType().ordinal());
                buf.writeUtf(entry.getFilterId(), 128);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, entry.getIconStack());
            }
        }
    }

    public static PacketSyncOutputFilter decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readByte() & 255;
        List<OutputInterfaceFilter.FilterEntry> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            boolean present = buf.readBoolean();
            if (present) {
                OutputInterfaceFilter.FilterType type = OutputInterfaceFilter.FilterType.byOrdinal(buf.readByte() & 255);
                String filterId = buf.readUtf(128);
                ItemStack icon = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                list.add(new OutputInterfaceFilter.FilterEntry(type, filterId, icon));
            } else {
                list.add(new OutputInterfaceFilter.FilterEntry());
            }
        }
        return new PacketSyncOutputFilter(list);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSyncOutputFilter> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null && player.containerMenu instanceof ContainerOutputInterface menu) {
                OutputInterfaceFilter filter = menu.getFilter();
                if (filter != null) {
                    for (int i = 0; i < entries.size() && i < OutputInterfaceFilter.FILTER_SLOTS; i++) {
                        OutputInterfaceFilter.FilterEntry entry = entries.get(i);
                        filter.setFilter(i, entry.getType(), entry.getFilterId(), entry.getIconStack());
                    }
                }
            }
        });
    }
}
