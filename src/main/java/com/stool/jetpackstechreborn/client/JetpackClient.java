package com.stool.jetpackstechreborn.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.stool.jetpackstechreborn.JetpackMod;
import com.stool.jetpackstechreborn.component.JetpackDataComponentTypes;
import com.stool.jetpackstechreborn.items.JetpackItem;
import com.stool.jetpackstechreborn.networking.JetpackBoostPayload;
import com.stool.jetpackstechreborn.networking.JetpackHoverTogglePayload;
import com.stool.jetpackstechreborn.networking.JetpackJumpPayload;
import com.stool.jetpackstechreborn.networking.JetpackTogglePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import techreborn.utils.TRItemUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.stool.jetpackstechreborn.JetpackMod.MOD_ID;

public class JetpackClient implements ClientModInitializer {
    public static final Category CATEGORY = new Category(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
    public static KeyMapping toggleKey;
    public static KeyMapping hoverToggleKey;
    public static KeyMapping boostKey;
    private boolean lastJumping = false;
    private boolean lastBoosting = false;

    private static final Map<UUID, JetpackSoundInstance> FLIGHT_SOUNDS = new HashMap<>();
    private static final Map<UUID, JetpackSoundInstance> HOVER_SOUNDS = new HashMap<>();
    private static final Map<UUID, LowBatterySoundInstance> LOW_BATTERY_SOUNDS = new HashMap<>();

    @Override
    public void onInitializeClient() {
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.jetpacks.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
        ));

        hoverToggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.jetpacks.hover_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
        ));

        boostKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.jetpacks.boost",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                ClientPlayNetworking.send(new JetpackTogglePayload());
            }
            while (hoverToggleKey.consumeClick()) {
                ClientPlayNetworking.send(new JetpackHoverTogglePayload());
            }

            if (client.player != null) {
                ItemStack chest = client.player.getItemBySlot(EquipmentSlot.CHEST);
                boolean hasActiveJetpack = chest.getItem() instanceof JetpackItem && TRItemUtils.isActive(chest);
                boolean jumping = client.options.keyJump.isDown();
                boolean boosting = boostKey.isDown();

                if (hasActiveJetpack) {
                    ClientPlayNetworking.send(new JetpackJumpPayload(jumping));
                    lastJumping = jumping;
                    if (boosting != lastBoosting) {
                        ClientPlayNetworking.send(new JetpackBoostPayload(boosting));
                        lastBoosting = boosting;
                    }
                } else {
                    if (lastJumping) {
                        ClientPlayNetworking.send(new JetpackJumpPayload(false));
                        lastJumping = false;
                    }
                    if (lastBoosting) {
                        ClientPlayNetworking.send(new JetpackBoostPayload(false));
                        lastBoosting = false;
                    }
                }

                updateSounds(client);
            }
        });
    }

    public static boolean hasHorizontalInput(Minecraft client) {
        return client.options.keyUp.isDown() || client.options.keyDown.isDown()
            || client.options.keyLeft.isDown() || client.options.keyRight.isDown();
    }

    private void updateSounds(Minecraft client) {
        // We only care about the local player for now, but system could be extended to other players
        Player player = client.player;
        if (player == null) return;

        UUID uuid = player.getUUID();
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasJetpack = chest.getItem() instanceof JetpackItem && TRItemUtils.isActive(chest);

        boolean steering = hasJetpack && !isThrusting(client) && isHovering(player)
            && !player.onGround() && hasHorizontalInput(client);

        boolean shouldFlight = hasJetpack && !player.onGround() && (isThrusting(client) || steering);
        if (shouldFlight) {
            if (!FLIGHT_SOUNDS.containsKey(uuid) || FLIGHT_SOUNDS.get(uuid).isStopped()) {
                JetpackSoundInstance instance = new JetpackSoundInstance(player, JetpackMod.FLIGHT_SOUND, false);
                FLIGHT_SOUNDS.put(uuid, instance);
                client.getSoundManager().play(instance);
            }
        }

        boolean shouldHover = hasJetpack && isHovering(player) && !isThrusting(client) && !player.onGround()
            && player.getDeltaMovement().y <= 0 && !steering;
        if (shouldHover) {
            if (!HOVER_SOUNDS.containsKey(uuid) || HOVER_SOUNDS.get(uuid).isStopped()) {
                JetpackSoundInstance instance = new JetpackSoundInstance(player, JetpackMod.HOVER_SOUND, true);
                HOVER_SOUNDS.put(uuid, instance);
                client.getSoundManager().play(instance);
            }
        }

        if (hasJetpack && !player.onGround()) {
            ItemStack jetpackStack = chest;
            JetpackItem jetpack = (JetpackItem) jetpackStack.getItem();
            long energy = jetpack.getStoredEnergy(jetpackStack);
            long capacity = jetpack.getEnergyCapacity(jetpackStack);
            boolean lowBattery = capacity > 0 && energy > 0 && (double) energy / capacity <= 0.10;
            if (lowBattery && (!LOW_BATTERY_SOUNDS.containsKey(uuid) || LOW_BATTERY_SOUNDS.get(uuid).isStopped())) {
                LowBatterySoundInstance instance = new LowBatterySoundInstance(player, JetpackMod.LOW_BATTERY_SOUND);
                LOW_BATTERY_SOUNDS.put(uuid, instance);
                client.getSoundManager().play(instance);
            }
        }
    }

    public static boolean isHovering(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof JetpackItem) {
            return !Boolean.FALSE.equals(chest.get(JetpackDataComponentTypes.HOVER_MODE));
        }
        return false;
    }

    public static boolean isThrusting(Minecraft client) {
        return client.options.keyJump.isDown();
    }

    public static boolean isBoosting() {
        return boostKey != null && boostKey.isDown();
    }
}
