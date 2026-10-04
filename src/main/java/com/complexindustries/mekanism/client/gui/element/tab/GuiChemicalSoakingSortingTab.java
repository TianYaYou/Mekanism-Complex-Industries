package com.complexindustries.mekanism.client.gui.element.tab;

import com.complexindustries.mekanism.content.soaker.TileEntityChemicalSoakingFactory;
import com.complexindustries.mekanism.network.MCIPacketHandler;
import com.complexindustries.mekanism.network.PacketToggleFactorySorting;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class GuiChemicalSoakingSortingTab extends GuiInsetElement<TileEntityChemicalSoakingFactory> {

    public GuiChemicalSoakingSortingTab(IGuiWrapper gui, TileEntityChemicalSoakingFactory tile) {
        super(MekanismUtils.getResource(ResourceType.GUI, "sorting.png"), gui, tile, -26, 62, 35, 18, true);
        setTooltip(MekanismLang.AUTO_SORT);
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        drawScrollingString(guiGraphics, OnOff.of(dataSource.isSorting()).getTextComponent(), 0, 24, TextAlignment.CENTER, titleTextColor(), 3, false);
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_FACTORY_SORT);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        MCIPacketHandler.sendToServer(new PacketToggleFactorySorting(dataSource.getBlockPos()));
    }
}
