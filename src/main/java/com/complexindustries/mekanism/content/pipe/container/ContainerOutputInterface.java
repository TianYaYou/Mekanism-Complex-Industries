package com.complexindustries.mekanism.content.pipe.container;

import com.complexindustries.mekanism.content.pipe.TileEntityIndustrialPipe;
import com.complexindustries.mekanism.content.pipe.TileEntityOutputInterface;
import com.complexindustries.mekanism.content.pipe.attachment.IPipeAttachment;
import com.complexindustries.mekanism.content.pipe.attachment.OutputInterfaceAttachment;
import com.complexindustries.mekanism.content.pipe.interfaces.IOutputInterface;
import com.complexindustries.mekanism.content.pipe.interfaces.OutputInterfaceFilter;
import com.complexindustries.mekanism.registration.MCIContainerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContainerOutputInterface extends AbstractContainerMenu {

    private final IOutputInterface interfaceInstance;
    private final BlockPos targetPos;
    private final boolean isAttachment;
    private final Direction attachedFace;
    private final ContainerData data;

    public static ContainerOutputInterface create(int windowId, Inventory inv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean isAttachment = buf.readBoolean();
        Direction face = isAttachment ? Direction.values()[buf.readByte() & 255] : null;
        BlockEntity be = inv.player.level().getBlockEntity(pos);

        IOutputInterface target = null;
        if (isAttachment && be instanceof TileEntityIndustrialPipe pipe) {
            IPipeAttachment att = pipe.getAttachment(face);
            if (att instanceof IOutputInterface out) {
                target = out;
            } else {
                OutputInterfaceAttachment clientAtt = new OutputInterfaceAttachment(pipe, face);
                pipe.addAttachment(face, clientAtt);
                target = clientAtt;
            }
        } else if (be instanceof TileEntityOutputInterface tile) {
            target = tile;
        }

        return new ContainerOutputInterface(windowId, inv, target, pos, isAttachment, face);
    }

    public ContainerOutputInterface(int windowId, Inventory playerInv, IOutputInterface interfaceInstance) {
        this(windowId, playerInv, interfaceInstance,
                interfaceInstance != null ? interfaceInstance.getInterfacePos() : BlockPos.ZERO,
                interfaceInstance != null && interfaceInstance.getAttachedFace() != null,
                interfaceInstance != null ? interfaceInstance.getAttachedFace() : null);
    }

    public ContainerOutputInterface(int windowId, Inventory playerInv, IOutputInterface interfaceInstance,
                                    BlockPos targetPos, boolean isAttachment, @Nullable Direction attachedFace) {
        super(MCIContainerTypes.OUTPUT_INTERFACE.get(), windowId);
        this.interfaceInstance = interfaceInstance;
        this.targetPos = targetPos;
        this.isAttachment = isAttachment;
        this.attachedFace = attachedFace;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return interfaceInstance != null ? interfaceInstance.getPriority() : 0;
            }

            @Override
            public void set(int index, int value) {
                if (interfaceInstance != null) {
                    interfaceInstance.setPriority(value);
                }
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
        addDataSlots(this.data);

        // Add player inventory slots (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 104 + row * 18));
            }
        }
        // Add player hotbar (1 row of 9)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 162));
        }
    }

    public IOutputInterface getInterfaceInstance() {
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

    public int getPriority() {
        return data.get(0);
    }

    public OutputInterfaceFilter getFilter() {
        if (interfaceInstance instanceof OutputInterfaceAttachment att) {
            return att.getFilter();
        } else if (interfaceInstance instanceof TileEntityOutputInterface tile) {
            return tile.getFilter();
        }
        return null;
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
