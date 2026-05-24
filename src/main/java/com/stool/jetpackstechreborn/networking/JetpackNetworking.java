package com.stool.jetpackstechreborn.networking;

import com.stool.jetpackstechreborn.JetpackMod;
import com.stool.jetpackstechreborn.items.JetpackItem;
import com.stool.jetpackstechreborn.items.JetpackMovement;
import com.stool.jetpackstechreborn.component.JetpackDataComponentTypes;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techreborn.component.TRDataComponentTypes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JetpackNetworking {
    private static final Map<UUID, Boolean> THRUSTING = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> BOOSTING = new ConcurrentHashMap<>();
    private static final Set<UUID> LOW_BATTERY_WARNED = ConcurrentHashMap.newKeySet();

    public static boolean isThrusting(Player player) {
        return THRUSTING.getOrDefault(player.getUUID(), false);
    }

    public static boolean isBoosting(Player player) {
        return BOOSTING.getOrDefault(player.getUUID(), false);
    }

    public static void warnLowBattery(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        LOW_BATTERY_WARNED.add(player.getUUID());
        serverPlayer.sendOverlayMessage(
            Component.translatable("jetpacks.message.battery_low").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
        );
    }

    public static void clearLowBatteryWarning(Player player) {
        LOW_BATTERY_WARNED.remove(player.getUUID());
    }

    public static void init() {
        PayloadTypeRegistry<RegistryFriendlyByteBuf> registry = PayloadTypeRegistry.serverboundPlay();
        registry.register(JetpackTogglePayload.ID, JetpackTogglePayload.CODEC);
        registry.register(JetpackHoverTogglePayload.ID, JetpackHoverTogglePayload.CODEC);
        registry.register(JetpackJumpPayload.ID, JetpackJumpPayload.CODEC);
        registry.register(JetpackBoostPayload.ID, JetpackBoostPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(JetpackTogglePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (!(context.player() instanceof ServerPlayer player)) return;
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chest.getItem() instanceof JetpackItem jetpack) {
                    boolean current = chest.getOrDefault(TRDataComponentTypes.IS_ACTIVE, false);
                    if (!current && jetpack.getStoredEnergy(chest) <= 0) {
                        player.sendOverlayMessage(
                            Component.translatable("jetpacks.message.depleted").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                        );
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            JetpackMod.LOW_BATTERY_SOUND, SoundSource.PLAYERS, 0.8f, 1.0f);
                        return;
                    }

                    boolean newState = !current;
                    chest.set(TRDataComponentTypes.IS_ACTIVE, newState);

                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        JetpackMod.SWITCH_SOUND, SoundSource.PLAYERS, 0.8f, newState ? 1.1f : 0.9f);
                    player.sendSystemMessage(
                        Component.translatable("jetpacks.message.toggle",
                            newState ? Component.translatable("reborncore.message.active").withStyle(ChatFormatting.GREEN) :
                                       Component.translatable("reborncore.message.inactive").withStyle(ChatFormatting.RED)),
                        true);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JetpackHoverTogglePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (!(context.player() instanceof ServerPlayer player)) return;
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chest.getItem() instanceof JetpackItem) {
                    boolean current = !Boolean.FALSE.equals(chest.get(JetpackDataComponentTypes.HOVER_MODE));
                    boolean newState = !current;
                    chest.set(JetpackDataComponentTypes.HOVER_MODE, newState);

                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        JetpackMod.SWITCH_SOUND, SoundSource.PLAYERS, 0.7f, newState ? 1.0f : 0.85f);
                    player.sendSystemMessage(
                        Component.translatable("jetpacks.message.hover_toggle",
                            newState ? Component.translatable("reborncore.message.active").withStyle(ChatFormatting.GREEN) :
                                       Component.translatable("reborncore.message.inactive").withStyle(ChatFormatting.RED)),
                        true);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JetpackJumpPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                THRUSTING.put(context.player().getUUID(), payload.jumping());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JetpackBoostPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BOOSTING.put(context.player().getUUID(), payload.boosting());
            });
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uuid = handler.getPlayer().getUUID();
            THRUSTING.remove(uuid);
            BOOSTING.remove(uuid);
            LOW_BATTERY_WARNED.remove(uuid);
            com.stool.jetpackstechreborn.items.JetpackMovement.clear(uuid);
        });
    }
}
