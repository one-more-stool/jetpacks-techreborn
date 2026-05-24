package com.stool.jetpackstechreborn.client;

import com.stool.jetpackstechreborn.items.JetpackItem;
import com.stool.jetpackstechreborn.items.JetpackTier;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class JetpackSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;
    private final boolean hover;
    private float boostFactor = 0.0f;

    public JetpackSoundInstance(Player player, SoundEvent soundEvent, boolean hover) {
        super(soundEvent, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.hover = hover;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.05f; // start quiet
        this.relative = false; // entity-based position
    }

    @Override
    public void tick() {
        if (player.isRemoved()) {
            this.stop();
            return;
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof JetpackItem jetpack)) {
            this.stop();
            return;
        }

        // monkeycode to determine if this specific sound should still be playing
        boolean shouldPlay;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        // jetpack should NOT make sound if player is on the ground
        if (player.onGround()) {
            shouldPlay = false;
        } else if (hover) {
            shouldPlay = JetpackClient.isHovering(player)
                && !JetpackClient.isThrusting(mc)
                && !JetpackClient.hasHorizontalInput(mc)
                && player.getDeltaMovement().y <= 0;
        } else {
            boolean steering = JetpackClient.isHovering(player)
                && !JetpackClient.isThrusting(mc)
                && JetpackClient.hasHorizontalInput(mc);
            shouldPlay = JetpackClient.isThrusting(mc) || steering;
        }

        if (shouldPlay) {
            this.x = (float) player.getX();
            this.y = (float) player.getY();
            this.z = (float) player.getZ();
            this.volume = Math.min(this.volume + 0.1f, hover ? 0.4f : 0.6f);

            boolean wantsBoost = !hover && JetpackClient.isBoosting() && jetpack.getJetpackTier().supportsBoost();
            float boostRampUp = 0.06f;
            float boostRampDown = 0.10f;
            if (wantsBoost) {
                boostFactor = Math.min(1.0f, boostFactor + boostRampUp);
            } else {
                boostFactor = Math.max(0.0f, boostFactor - boostRampDown);
            }
            float targetPitch = 1.0f + 0.35f * boostFactor;
            this.pitch += (targetPitch - this.pitch) * 0.25f;
        } else {
            this.volume -= 0.1f;
            if (this.volume <= 0.0f) {
                this.stop();
            }
        }
    }
}
