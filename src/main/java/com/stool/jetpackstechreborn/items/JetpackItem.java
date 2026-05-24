package com.stool.jetpackstechreborn.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.phys.Vec3;
import reborncore.api.items.ArmorBlockEntityTicker;
import techreborn.component.TRDataComponentTypes;
import techreborn.utils.TRItemUtils;
import com.stool.jetpackstechreborn.component.JetpackDataComponentTypes;
import com.stool.jetpackstechreborn.networking.JetpackNetworking;

import java.util.function.Consumer;

public class JetpackItem extends JetpackEnergyArmourItem implements ArmorBlockEntityTicker {
    private static final double PARTICLE_BACK_OFFSET = 0.75;
    private static final double PARTICLE_SIDE_OFFSET = 0.26;
    private static final double PARTICLE_HEIGHT = 0.22;

    private final JetpackTier tier;

    public JetpackItem(JetpackTier tier) {
        super(tier.armorMaterial, ArmorType.CHESTPLATE, tier.getCapacity(), tier.energyTier, tier.name + "_jetpack");
        this.tier = tier;
    }

    public JetpackTier getJetpackTier() {
        return tier;
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return tier.getCapacity();
    }

    @Override
    public void setStoredEnergy(ItemStack stack, long amount) {
        super.setStoredEnergy(stack, Math.min(amount, getEnergyCapacity(stack)));
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return 0;
    }

    @Override
    public InteractionResult use(net.minecraft.world.level.Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.setItemSlot(EquipmentSlot.CHEST, stack.copyAndClear());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tickArmor(ItemStack stack, boolean hasFullSuit, Player player) {
        if (player.level().isClientSide()) return;

        boolean isActive = TRItemUtils.isActive(stack);
        if (!isActive) return;

        long energy = getStoredEnergy(stack);
        long capacity = getEnergyCapacity(stack);

        if (capacity > 0) {
            double ratio = (double) energy / capacity;
            if (ratio <= 0.10) {
                JetpackNetworking.warnLowBattery(player);
            } else {
                JetpackNetworking.clearLowBatteryWarning(player);
            }
        }

        if (energy <= 0) {
            if (stack.getOrDefault(TRDataComponentTypes.IS_ACTIVE, false)) {
                stack.set(TRDataComponentTypes.IS_ACTIVE, false);
                player.sendSystemMessage(Component.translatable("jetpacks.message.out_of_energy").withStyle(ChatFormatting.RED));
            }
            return;
        }

        boolean isThrusting = JetpackNetworking.isThrusting(player);
        boolean isHoverMode = !Boolean.FALSE.equals(stack.get(JetpackDataComponentTypes.HOVER_MODE));

        boolean hasHorizontalInput = false;
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            net.minecraft.world.entity.player.Input in = sp.getLastClientInput();
            if (in != null) {
                hasHorizontalInput = in.forward() || in.backward() || in.left() || in.right();
            }
        }

        Vec3 v = player.getDeltaMovement();
        double y = v.y;
        boolean applied = false;

        if (isThrusting) {
            if (tryUseEnergy(stack, tier.getEnergyCost())) {
                y = Math.min(y + tier.getThrust(), 0.6);
                player.resetFallDistance();
                applied = true;
                spawnFlightParticles(player, false);
            }
        } else if (isHoverMode && !player.onGround() && y <= tier.getHoverActivationVelocity()) {
            boolean steering = hasHorizontalInput;
            int cost = steering ? tier.getEnergyCost() : tier.getHoverCost();
            if (tryUseEnergy(stack, cost)) {
                double hoverDescent = tier.getHoverDescent();
                if (player.isShiftKeyDown()) {
                    hoverDescent *= 5.0;
                }

                double targetDescent = -hoverDescent;
                if (y < targetDescent) {
                    y = targetDescent;
                } else {
                    y += (targetDescent - y) * 0.25;
                }

                player.resetFallDistance();
                applied = true;
                spawnFlightParticles(player, !steering);
            }
        }

        if (applied) {
            player.setDeltaMovement(v.x, y, v.z);
            player.hurtMarked = true;
        }

        boolean shouldGlide = !player.onGround() && (isThrusting || (isHoverMode && hasHorizontalInput && y <= tier.getHoverActivationVelocity()));
        if (shouldGlide) {
            player.setSharedFlag(7, true);
        } else if (player.isFallFlying()) {
            player.setSharedFlag(7, false);
        }
    }

    void spawnFlightParticles(Player player, boolean hover) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        float yaw = player.yBodyRot * ((float) Math.PI / 180F);
        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);

        double lookX = -sinYaw;
        double lookZ = cosYaw;
        double backX = player.getX() - lookX * PARTICLE_BACK_OFFSET;
        double backZ = player.getZ() - lookZ * PARTICLE_BACK_OFFSET;
        double nozzleY = player.getY() + PARTICLE_HEIGHT;

        double leftX = backX - PARTICLE_SIDE_OFFSET * sinYaw;
        double leftZ = backZ + PARTICLE_SIDE_OFFSET * cosYaw;
        double rightX = backX + PARTICLE_SIDE_OFFSET * sinYaw;
        double rightZ = backZ - PARTICLE_SIDE_OFFSET * cosYaw;

        double exhaustX = lookX * 0.03;
        double exhaustZ = lookZ * 0.03;

        if (hover) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, backX, nozzleY, backZ, 1, exhaustX, 0.01, exhaustZ, 0.001);
        } else {
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, leftX, nozzleY, leftZ, 1, exhaustX, -0.04, exhaustZ, 0.002);
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, rightX, nozzleY, rightZ, 1, exhaustX, -0.04, exhaustZ, 0.002);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type) {
        TRItemUtils.buildActiveTooltip(stack, tooltip);

        boolean isHoverMode = !Boolean.FALSE.equals(stack.get(JetpackDataComponentTypes.HOVER_MODE));
        tooltip.accept(Component.translatable("jetpacks.tooltip.hover_mode")
            .append(": ")
            .append(isHoverMode ?
                Component.translatable("reborncore.message.active").withStyle(ChatFormatting.GREEN) :
                Component.translatable("reborncore.message.inactive").withStyle(ChatFormatting.RED)));
    }
}
