package com.stool.jetpackstechreborn;

import com.stool.jetpackstechreborn.component.JetpackDataComponentTypes;
import com.stool.jetpackstechreborn.config.JetpackConfigHandler;
import com.stool.jetpackstechreborn.items.JetpackItem;
import com.stool.jetpackstechreborn.items.JetpackMovement;
import com.stool.jetpackstechreborn.items.JetpackTier;
import com.stool.jetpackstechreborn.networking.JetpackNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import techreborn.utils.TRItemUtils;

public class JetpackMod implements ModInitializer {
    public static final String MOD_ID = "jetpacks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static JetpackItem SIMPLE_JETPACK;
    public static JetpackItem ADVANCED_JETPACK;
    public static JetpackItem INDUSTRIAL_JETPACK;
    public static JetpackItem ULTIMATE_JETPACK;

    // crafting components
    public static Item BASIC_THRUSTER;
    public static Item ADVANCED_THRUSTER;
    public static Item ENERGY_CONTROLLER;
    public static Item REINFORCED_FRAME;

    public static final SoundEvent FLIGHT_SOUND = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "flight"));
    public static final SoundEvent HOVER_SOUND = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "hover"));
    public static final SoundEvent SWITCH_SOUND = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "switch"));
    public static final SoundEvent LOW_BATTERY_SOUND = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "lowbattery"));

    private static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "item_group"));

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing TechReborn Jetpacks");

        JetpackConfigHandler.load();
        JetpackDataComponentTypes.init();

        Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "flight"), FLIGHT_SOUND);
        Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "hover"), HOVER_SOUND);
        Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "switch"), SWITCH_SOUND);
        Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "lowbattery"), LOW_BATTERY_SOUND);

        // items
        SIMPLE_JETPACK = register("simple_jetpack", new JetpackItem(JetpackTier.SIMPLE));
        ADVANCED_JETPACK = register("advanced_jetpack", new JetpackItem(JetpackTier.ADVANCED));
        INDUSTRIAL_JETPACK = register("industrial_jetpack", new JetpackItem(JetpackTier.INDUSTRIAL));
        ULTIMATE_JETPACK = register("ultimate_jetpack", new JetpackItem(JetpackTier.ULTIMATE));

        BASIC_THRUSTER = registerItem("basic_thruster", new Item(new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(MOD_ID, "basic_thruster")))));
        ADVANCED_THRUSTER = registerItem("advanced_thruster", new Item(new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(MOD_ID, "advanced_thruster")))));
        ENERGY_CONTROLLER = registerItem("energy_controller", new Item(new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(MOD_ID, "energy_controller")))));
        REINFORCED_FRAME = registerItem("reinforced_frame", new Item(new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(MOD_ID, "reinforced_frame")))));

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP_KEY, FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.jetpacks"))
            .icon(() -> new ItemStack(ULTIMATE_JETPACK))
            .build());

        CreativeModeTabEvents.modifyOutputEvent(ITEM_GROUP_KEY).register(this::addItems);

        JetpackNetworking.init();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chest.getItem() instanceof JetpackItem jetpack && TRItemUtils.isActive(chest)) {
                    JetpackMovement.applyAirMobility(player, chest, jetpack);
                }
            }
        });
    }

    private void addItems(FabricCreativeModeTabOutput entries) {
        entries.accept(SIMPLE_JETPACK);
        entries.accept(ADVANCED_JETPACK);
        entries.accept(INDUSTRIAL_JETPACK);
        entries.accept(ULTIMATE_JETPACK);
        entries.accept(BASIC_THRUSTER);
        entries.accept(ADVANCED_THRUSTER);
        entries.accept(ENERGY_CONTROLLER);
        entries.accept(REINFORCED_FRAME);
    }

    private <T extends JetpackItem> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name), item);
    }

    private Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name), item);
    }
}
