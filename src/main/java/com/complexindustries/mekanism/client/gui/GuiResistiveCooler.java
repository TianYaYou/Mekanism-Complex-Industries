package com.complexindustries.mekanism.client.gui;

import com.complexindustries.mekanism.content.tile.TileEntityResistiveCooler;
import com.complexindustries.mekanism.inventory.container.ContainerResistiveCooler;
import com.complexindustries.mekanism.network.MCIPacketHandler;
import com.complexindustries.mekanism.network.PacketSetCoolerEnergy;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class GuiResistiveCooler extends GuiMekanismTile<TileEntityResistiveCooler, ContainerResistiveCooler> {
    private GuiTextField energyUsageField;

    public GuiResistiveCooler(ContainerResistiveCooler container, Inventory inv, Component title) {
        super(container, inv, title);
        this.dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        // 1. Black Terminal Screen (48, 23, 80, 42)
        addRenderableWidget(new GuiInnerScreen(this, 48, 23, 80, 42, () -> {
            double ambient = tile.getAmbientTemperature(null);
            double temp = tile.getTotalTemperature();
            double belowAmbient = Math.max(0.0, ambient - temp);
            return List.of(
                    Component.translatable("gui.mekanism_complex_industries.resistive_cooler.below_ambient",
                            MekanismUtils.getTemperatureDisplay(belowAmbient, TemperatureUnit.KELVIN, false)),
                    MekanismLang.RESISTIVE_HEATER_USAGE.translate(EnergyDisplay.of(tile.getEnergyContainer().getEnergyPerTick()))
            );
        }).clearFormat().tooltip(() -> {
            double ambient = tile.getAmbientTemperature(null);
            double temp = tile.getTotalTemperature();
            double belowAmbient = Math.max(0.0, ambient - temp);
            return List.of(
                    Component.translatable("gui.mekanism_complex_industries.resistive_cooler.temp",
                            MekanismUtils.getTemperatureDisplay(temp, TemperatureUnit.KELVIN, true)),
                    Component.translatable("gui.mekanism_complex_industries.resistive_cooler.below_ambient_full",
                            MekanismUtils.getTemperatureDisplay(belowAmbient, TemperatureUnit.KELVIN, false)),
                    Component.translatable("gui.mekanism_complex_industries.resistive_cooler.ambient_temp",
                            MekanismUtils.getTemperatureDisplay(ambient, TemperatureUnit.KELVIN, true))
            );
        }));

        // 2. Vertical Power Bar on Right
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));

        // 3. Energy Tab on Right Edge
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getEnergyUsed));

        // 4. Heat Tab on Right Edge
        addRenderableWidget(new GuiHeatTab(this, () -> List.of(
                MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTotalTemperature(), TemperatureUnit.KELVIN, true)),
                MekanismLang.TRANSFERRED_RATE.translate(MekanismUtils.getTemperatureDisplay(tile.getLastTransferLoss(), TemperatureUnit.KELVIN, false)),
                MekanismLang.DISSIPATED_RATE.translate(MekanismUtils.getTemperatureDisplay(tile.getLastEnvironmentLoss(), TemperatureUnit.KELVIN, false))
        )));

        // 5. Configurable Power / Cooling Rate Input Field (50, 51, 76, 12) with Checkmark Button
        energyUsageField = addRenderableWidget(new GuiTextField(this, 50, 51, 76, 12));
        energyUsageField.setMaxLength(7);
        energyUsageField.setInputValidator(InputValidator.DIGIT);
        energyUsageField.configureDigitalInput(this::setEnergyUsage);
        energyUsageField.setFocused(true);
    }

    @Override
    protected void drawForegroundText(GuiGraphics graphics, int mouseX, int mouseY) {
        renderTitleText(graphics);
        drawString(graphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(graphics, mouseX, mouseY);
    }

    private void setEnergyUsage() {
        if (!energyUsageField.getText().isEmpty()) {
            try {
                FloatingLong enteredEnergy = FloatingLong.parseFloatingLong(energyUsageField.getText());
                FloatingLong inJoules = MekanismUtils.convertToJoules(enteredEnergy);
                MCIPacketHandler.INSTANCE.sendToServer(new PacketSetCoolerEnergy(tile.getBlockPos(), inJoules));
            } catch (NumberFormatException ignored) {}
            energyUsageField.setText("");
        }
    }
}
