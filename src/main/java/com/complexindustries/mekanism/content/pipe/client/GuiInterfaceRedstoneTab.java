package com.complexindustries.mekanism.content.pipe.client;

import com.complexindustries.mekanism.network.MCIPacketHandler;
import com.complexindustries.mekanism.network.PacketSetRedstoneMode;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiInterfaceRedstoneTab extends GuiInsetElement<Void> {

    private static final ResourceLocation DISABLED = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_disabled.png");
    private static final ResourceLocation HIGH = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_high.png");
    private static final ResourceLocation LOW = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_low.png");

    private final Supplier<RedstoneControl> modeSupplier;
    private final Consumer<RedstoneControl> modeConsumer;
    private final BlockPos pos;
    private final boolean isAttachment;
    private final Direction face;
    private final Map<RedstoneControl, Tooltip> tooltips = new EnumMap<>(RedstoneControl.class);

    public GuiInterfaceRedstoneTab(IGuiWrapper gui, BlockPos pos, boolean isAttachment, @Nullable Direction face,
                                  Supplier<RedstoneControl> modeSupplier, Consumer<RedstoneControl> modeConsumer) {
        super(DISABLED, gui, null, gui.getXSize(), 6, 26, 18, false);
        this.pos = pos;
        this.isAttachment = isAttachment;
        this.face = face;
        this.modeSupplier = modeSupplier;
        this.modeConsumer = modeConsumer;
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        RedstoneControl mode = modeSupplier.get();
        if (mode == null) {
            mode = RedstoneControl.DISABLED;
        }
        setTooltip(tooltips.computeIfAbsent(mode, type -> TooltipUtils.create(type.getTextComponent())));
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        RedstoneControl current = modeSupplier.get();
        if (current == null) {
            current = RedstoneControl.DISABLED;
        }
        RedstoneControl next;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            next = switch (current) {
                case DISABLED -> RedstoneControl.HIGH;
                case HIGH -> RedstoneControl.LOW;
                case LOW, PULSE -> RedstoneControl.DISABLED;
            };
        } else {
            next = switch (current) {
                case DISABLED -> RedstoneControl.LOW;
                case LOW -> RedstoneControl.HIGH;
                case HIGH, PULSE -> RedstoneControl.DISABLED;
            };
        }
        modeConsumer.accept(next);
        MCIPacketHandler.sendToServer(new PacketSetRedstoneMode(pos, isAttachment, face, next));
    }

    @Override
    public boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    protected ResourceLocation getOverlay() {
        RedstoneControl mode = modeSupplier.get();
        if (mode == null) {
            return DISABLED;
        }
        return switch (mode) {
            case HIGH -> HIGH;
            case LOW -> LOW;
            default -> DISABLED;
        };
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_REDSTONE_CONTROL);
    }
}
