package com.stool.jetpackstechreborn.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;
import reborncore.common.powerSystem.RcEnergyItem;
import reborncore.common.powerSystem.RcEnergyTier;
import reborncore.common.util.ItemUtils;
import techreborn.component.TRDataComponentTypes;
import techreborn.init.TRItemSettings;

import static com.stool.jetpackstechreborn.JetpackMod.MOD_ID;

public abstract class JetpackEnergyArmourItem extends Item implements RcEnergyItem {
    public final long maxCharge;
    private final RcEnergyTier energyTier;

    public JetpackEnergyArmourItem(ArmorMaterial material, ArmorType slot, long maxCharge, RcEnergyTier energyTier, String name) {
        super(buildProperties(material, slot, name));
        this.maxCharge = maxCharge;
        this.energyTier = energyTier;
    }

    private static Item.Properties buildProperties(ArmorMaterial material, ArmorType slot, String name) {
        return new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(MOD_ID, name)))
                .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                .component(DataComponents.TOOLTIP_DISPLAY, TRItemSettings.UNBREAKABLE_HIDE)
                .stacksTo(1)
                .humanoidArmor(material, slot)
                .component(TRDataComponentTypes.IS_ACTIVE, false);
    }

    // item props
    @Override
    public int getBarWidth(ItemStack stack) {
        return ItemUtils.getPowerForDurabilityBar(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ItemUtils.getColorForDurabilityBar(stack);
    }

    // RcEnergyItem
    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return maxCharge;
    }

    @Override
    public boolean allowComponentsUpdateAnimation(Player player, net.minecraft.world.InteractionHand hand, ItemStack original, ItemStack updated) {
        return false;
    }

    @Override
    public boolean allowContinuingBlockBreaking(Player player, ItemStack oldStack, ItemStack newStack) {
        return true;
    }

    @Override
    public RcEnergyTier getTier() {
        return energyTier;
    }

    @Nullable
    public EquipmentSlot getSlotType() {
        Equippable equippableComponent = this.components().get(DataComponents.EQUIPPABLE);
        return equippableComponent != null ? equippableComponent.slot() : null;
    }
}
