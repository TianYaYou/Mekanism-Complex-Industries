package com.complexindustries.mekanism.content.block;

import java.util.List;
import java.util.Map.Entry;
import mekanism.api.Upgrade;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.security.SecurityObject;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MCIBlockItem extends ItemBlockMekanism<Block> implements ICapabilityAware {

    @Nullable
    private final ILangEntry description;
    private final boolean hasDetails;

    public MCIBlockItem(Block block, Properties properties) {
        this(block, properties, (ILangEntry) null, false);
    }

    public MCIBlockItem(Block block, Properties properties, String descriptionKey) {
        this(block, properties, descriptionKey != null ? () -> descriptionKey : null, false);
    }

    public MCIBlockItem(Block block, Properties properties, String descriptionKey, boolean hasDetails) {
        this(block, properties, descriptionKey != null ? () -> descriptionKey : null, hasDetails);
    }

    public MCIBlockItem(Block block, Properties properties, @Nullable ILangEntry description, boolean hasDetails) {
        super(block, properties);
        this.description = description;
        this.hasDetails = hasDetails;
    }

    @Nullable
    @Override
    public mekanism.api.tier.ITier getTier() {
        mekanism.common.block.attribute.AttributeTier<?> attributeTier = Attribute.get(getBlock(), mekanism.common.block.attribute.AttributeTier.class);
        if (attributeTier != null) {
            return attributeTier.tier();
        }
        return super.getTier();
    }

    @Nullable
    public ILangEntry getBlockDescription() {
        if (description != null) {
            return description;
        }
        if (getBlock() instanceof IHasDescription hasDescription) {
            return hasDescription.getDescription();
        }
        return null;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ILangEntry desc = getBlockDescription();
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            if (desc != null) {
                tooltip.add(desc.translate());
            }
        } else if (hasDetails && MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addDetails(stack, context, tooltip, flag);
        } else {
            addStats(stack, context, tooltip, flag);
            if (hasDetails) {
                tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
            if (desc != null) {
                tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
            }
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    }

    protected void addDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        // 1. Security and Owner
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);

        // Factory Type
        if (getBlock() instanceof ChemicalSoakingFactoryBlock) {
            tooltip.add(MekanismLang.FACTORY_TYPE.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                    Component.translatable("gui.mekanism_complex_industries.chemical_soaking.type")));
        } else if (getBlock() instanceof ChemicalSolidifierFactoryBlock) {
            tooltip.add(MekanismLang.FACTORY_TYPE.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                    Component.translatable("gui.mekanism_complex_industries.chemical_solidifier.type")));
        }

        // 2. Energy
        AttributeEnergy attributeEnergy = Attribute.get(getBlock(), AttributeEnergy.class);
        if (attributeEnergy != null) {
            long stored = StorageUtils.getStoredEnergyFromAttachment(stack);
            long max = attributeEnergy.getStorage();
            UpgradeAware upgradeAware = stack.get(MekanismDataComponents.UPGRADES);
            if (upgradeAware != null) {
                max = MekanismUtils.getMaxEnergy(upgradeAware.getUpgradeCount(Upgrade.ENERGY), max);
            }
            tooltip.add(MekanismLang.STORED_ENERGY.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY,
                    EnergyDisplay.of(stored, max)));
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, false);
        }

        // 3. Chemical
        StorageUtils.addStoredChemical(stack, tooltip);

        // 4. Fluid
        FluidStack fluidStack = StorageUtils.getStoredFluidFromAttachment(stack);
        if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, TextUtils.format(fluidStack.getAmount())));
        }

        // 5. Inventory
        if (Attribute.has(getBlock(), AttributeInventory.class) && ContainerType.ITEM.supports(stack)) {
            tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.hasInventory(stack)));
        }

        // 6. Upgrades
        if (Attribute.has(getBlock(), AttributeUpgradeSupport.class)) {
            UpgradeAware upgradeAware = stack.get(MekanismDataComponents.UPGRADES);
            if (upgradeAware != null && !upgradeAware.upgrades().isEmpty()) {
                for (Entry<Upgrade, Integer> entry : upgradeAware.upgrades().entrySet()) {
                    tooltip.add(UpgradeDisplay.of(entry.getKey(), entry.getValue()).getTextComponent());
                }
            }
        }
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (Attribute.has(getBlock(), AttributeSecurity.class)) {
            event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (stack, ctx) -> new SecurityObject(stack), this);
            event.registerItem(IItemSecurityUtils.INSTANCE.securityCapability(), (stack, ctx) -> new SecurityObject(stack), this);
        }
    }
}
