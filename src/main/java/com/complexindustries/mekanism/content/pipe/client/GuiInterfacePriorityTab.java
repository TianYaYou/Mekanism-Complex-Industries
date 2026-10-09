package com.complexindustries.mekanism.content.pipe.client;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class GuiInterfacePriorityTab extends GuiInsetElement<Void> {

    private static final ResourceLocation ICON = MekanismUtils.getResource(ResourceType.GUI, "configuration.png");

    private final BlockPos pos;
    private final boolean isAttachment;
    private final Direction face;
    private final IntSupplier prioritySupplier;
    private final IntConsumer priorityConsumer;

    public GuiInterfacePriorityTab(IGuiWrapper gui, BlockPos pos, boolean isAttachment, @Nullable Direction face,
                                   IntSupplier prioritySupplier, IntConsumer priorityConsumer) {
        super(ICON, gui, null, gui.getXSize(), 34, 26, 18, false);
        this.pos = pos;
        this.isAttachment = isAttachment;
        this.face = face;
        this.prioritySupplier = prioritySupplier;
        this.priorityConsumer = priorityConsumer;
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(TooltipUtils.create(Component.literal("优先级配置 (当前: " + prioritySupplier.getAsInt() + ")")));
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        gui().addWindow(new GuiPriorityWindow(gui(), pos, isAttachment, face, prioritySupplier, priorityConsumer));
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_CONFIGURATION);
    }
}
