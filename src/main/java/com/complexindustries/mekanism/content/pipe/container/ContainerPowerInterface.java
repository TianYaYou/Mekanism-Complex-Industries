package com.complexindustries.mekanism.content.pipe.container;

import com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe;
import com.complexindustries.mekanism.content.pipe.TileEntityPowerInterface;
import com.complexindustries.mekanism.content.pipe.attachment.IPipeAttachment;
import com.complexindustries.mekanism.content.pipe.attachment.PowerInterfaceAttachment;
import com.complexindustries.mekanism.content.pipe.interfaces.IPowerInterface;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContainerPowerInterface extends AbstractContainerMenu {

    private final IPowerInterface interfaceInstance;
    private final BlockPos targetPos;
    private final boolean isAttachment;
    private final Direction attachedFace;

    public static ContainerPowerInterface create(int windowId, Inventory inv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean isAttachment = buf.readBoolean();
        Direction face = isAttachment ? Direction.values()[buf.readByte() & 255] : null;
        BlockEntity be = inv.player.level().getBlockEntity(pos);

        IPowerInterface target = null;
        if (isAttachment && be instanceof TileEntityIndustrialPipe pipe) {
            IPipeAttachment att = pipe.getAttachment(face);
            if (att instanceof IPowerInterface pwr) {
                target = pwr;
            } else {
                PowerInterfaceAttachment clientAtt = new PowerInterfaceAttachment(pipe, face);
                pipe.addAttachment(face, clientAtt);
                target = clientAtt;
            }
        } else if (be instanceof TileEntityPowerInterface tile) {
            target = tile;
        }

        return new ContainerPowerInterface(windowId, inv, target, pos, isAttachment, face);
    }

    public ContainerPowerInterface(int windowId, Inventory playerInv, IPowerInterface interfaceInstance) {
        this(windowId, playerInv, interfaceInstance,
                interfaceInstance != null ? interfaceInstance.getInterfacePos() : BlockPos.ZERO,
                interfaceInstance != null && interfaceInstance.getAttachedFace() != null,
                interfaceInstance != null ? interfaceInstance.getAttachedFace() : null);
    }

    public ContainerPowerInterface(int windowId, Inventory playerInv, IPowerInterface interfaceInstance,
                                   BlockPos targetPos, boolean isAttachment, @Nullable Direction attachedFace) {
        super(MCIContainerTypes.POWER_INTERFACE.get(), windowId);
        this.interfaceInstance = interfaceInstance;
        this.targetPos = targetPos;
        this.isAttachment = isAttachment;
        this.attachedFace = attachedFace;

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public IPowerInterface getInterfaceInstance() {
        return interfaceInstance;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public boolean isAttachment() {
        return isAttachment;
    }

    public Direction getAttachedFace() {
        return attachedFace;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5) <= 64.0;
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }
}
