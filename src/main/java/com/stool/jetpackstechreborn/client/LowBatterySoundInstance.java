package com.stool.jetpackstechreborn.client;

import com.stool.jetpackstechreborn.items.JetpackItem;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LowBatterySoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public LowBatterySoundInstance(Player player, SoundEvent soundEvent) {
        super(soundEvent, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.7f;
        this.pitch = 1.0f;
        this.relative = true;
        this.attenuation = Attenuation.NONE;
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }

    @Override
    public void tick() {
        if (player.isRemoved() || player.onGround()) {
            this.stop();
            return;
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof JetpackItem jetpack)) {
            this.stop();
            return;
        }

        long energy = jetpack.getStoredEnergy(chest);
        long capacity = jetpack.getEnergyCapacity(chest);
        if (capacity <= 0 || energy <= 0 || (double) energy / capacity > 0.10) {
            this.stop();
        }
    }
}
