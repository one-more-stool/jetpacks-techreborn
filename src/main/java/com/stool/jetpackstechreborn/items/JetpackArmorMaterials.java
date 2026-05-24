package com.stool.jetpackstechreborn.items;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

import static com.stool.jetpackstechreborn.JetpackMod.MOD_ID;

public class JetpackArmorMaterials {
    private static final TagKey<Item> EMPTY = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("techreborn", "empty"));

    public static final ArmorMaterial SIMPLE = register("simple_jetpack", 5, 33);
    public static final ArmorMaterial ADVANCED = register("advanced_jetpack", 5, 33);
    public static final ArmorMaterial INDUSTRIAL = register("industrial_jetpack", 5, 33);
    public static final ArmorMaterial ULTIMATE = register("ultimate_jetpack", 5, 33);

    private static ArmorMaterial register(String id, int defense, int durability) {
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(MOD_ID, id));
        EnumMap<ArmorType, Integer> defenseMap = new EnumMap<>(ArmorType.class);
        defenseMap.put(ArmorType.CHESTPLATE, defense);
        return new ArmorMaterial(durability, defenseMap, 10, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0f, 0.0f, EMPTY, asset);
    }
}
