package com.complexindustries.mekanism.client.gui.element;

import mekanism.api.Upgrade;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.common.MekanismLang;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class MCISupportedUpgrades extends GuiElement {

    private static final List<Upgrade> SUPPORTED = List.of(
            Upgrade.SPEED,
            Upgrade.ENERGY,
            Upgrade.MUFFLING,
            Upgrade.STONE_GENERATOR
    );

    private final Set<Upgrade> supportedUpgrades;

    public MCISupportedUpgrades(IGuiWrapper gui, int x, int y, Set<Upgrade> supportedUpgrades) {
        super(gui, x, y, 125, 14);
        this.supportedUpgrades = supportedUpgrades;
    }

    private int getUpgradeStartX() {
        int labelWidth = getFont().width(MekanismLang.UPGRADES_SUPPORTED.translate());
        return Math.max(34, labelWidth + 6);
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(graphics, mouseX, mouseY, partialTicks);
        renderBackgroundTexture(graphics, GuiElementHolder.HOLDER, 32, 32);

        int startX = getUpgradeStartX();
        for (int i = 0; i < SUPPORTED.size(); i++) {
            Upgrade upgrade = SUPPORTED.get(i);
            if (supportedUpgrades == null || supportedUpgrades.contains(upgrade)) {
                gui().renderItem(graphics, UpgradeUtils.getStack(upgrade), relativeX + 1 + startX + i * 14, relativeY + 1, 0.75F);
            }
        }
    }

    @Override
    public void renderForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderForeground(graphics, mouseX, mouseY);
        drawTextScaledBound(graphics, MekanismLang.UPGRADES_SUPPORTED.translate(), relativeX + 2, relativeY + 3, titleTextColor(), 54.0F);
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderToolTip(graphics, mouseX, mouseY);
        int startX = getUpgradeStartX();
        for (int i = 0; i < SUPPORTED.size(); i++) {
            Upgrade upgrade = SUPPORTED.get(i);
            if (supportedUpgrades != null && !supportedUpgrades.contains(upgrade)) {
                continue;
            }
            int itemX = getX() + 1 + startX + i * 14;
            int itemY = getY() + 1;
            if (mouseX >= itemX && mouseX < itemX + 12 && mouseY >= itemY && mouseY < itemY + 12) {
                Component title = MekanismLang.UPGRADE_TYPE.translateColored(EnumColor.YELLOW, upgrade);
                Component capacity = APILang.UPGRADE_MAX_INSTALLED.translate(upgrade.getMax());
                Component desc = upgrade.getDescription();
                displayTooltips(graphics, mouseX, mouseY, title, capacity, desc);
                return;
            }
        }
    }
}
