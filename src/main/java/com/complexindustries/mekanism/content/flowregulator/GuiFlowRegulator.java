package com.complexindustries.mekanism.content.flowregulator;

import com.complexindustries.mekanism.network.PacketSetFlowRate;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiFlowRegulator extends GuiMekanismTile<TileEntityFlowRegulator, ContainerFlowRegulator> {

    private GuiTextField flowRateField;

    public GuiFlowRegulator(ContainerFlowRegulator container, Inventory inv, Component title) {
        super(container, inv, title);
        this.dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Chemical Buffer Gauge on Left (26, 16)
        addRenderableWidget(new GuiChemicalGauge(
                () -> tile.bufferTank,
                () -> tile.getChemicalTanks(null),
                GaugeType.STANDARD, this, 26, 16
        ));

        // 2. Terminal Screen in Middle (53, 18, 105, 34)
        addRenderableWidget(new GuiInnerScreen(this, 53, 18, 105, 34, () -> List.of(
                Component.translatable("gui.mekanism_complex_industries.flow_regulator.limit",
                        TextUtils.format(tile.getFlowRate()) + " mB/s"),
                Component.translatable("gui.mekanism_complex_industries.flow_regulator.actual",
                        TextUtils.format(tile.getLastTransferredRate()) + " mB/s")
        )).spacing(2));

        // 3. Flow Rate Input Field (55, 54, 76, 12)
        flowRateField = addRenderableWidget(new GuiTextField(this, 55, 54, 76, 12));
        flowRateField.setMaxLength(9);
        flowRateField.setInputValidator(InputValidator.DIGIT);
        flowRateField.configureDigitalInput(this::setFlowRate);
        flowRateField.setFocused(true);

        // 4. Redstone Control Tab on Right
        addRenderableWidget(new GuiRedstoneControlTab(this, tile));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        renderTitleText(graphics);
        renderInventoryText(graphics);
        super.drawForegroundText(graphics, mouseX, mouseY);
    }

    private void setFlowRate() {
        if (!flowRateField.getText().isEmpty()) {
            try {
                long enteredRate = Math.max(0, Long.parseLong(flowRateField.getText()));
                PacketUtils.sendToServer(new PacketSetFlowRate(tile.getBlockPos(), enteredRate));
            } catch (NumberFormatException ignored) {}
            flowRateField.setText("");
        }
    }
}
