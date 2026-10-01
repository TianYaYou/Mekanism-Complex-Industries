package com.complexindustries.mekanism.client.gui.element.bar;

import com.complexindustries.mekanism.MCIConstants;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GuiCryoRateBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation CRYO_BAR = ResourceLocation.fromNamespaceAndPath(MCIConstants.MODID, "textures/gui/bar/horizontal_rate_cryo.png");
    private static final int texWidth = 78;
    private static final int texHeight = 8;

    public GuiCryoRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        super(CRYO_BAR, gui, handler, x, y, texWidth, texHeight, true);
    }

    @Override
    protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int displayInt = (int) (handlerLevel * texWidth);
        if (displayInt > 0) {
            guiGraphics.blit(getResource(), relativeX + 1, relativeY + 1, 0, 0, displayInt, texHeight, texWidth, texHeight);
        }
    }
}
