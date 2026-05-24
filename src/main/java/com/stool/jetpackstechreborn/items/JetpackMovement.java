package com.stool.jetpackstechreborn.items;

import com.stool.jetpackstechreborn.networking.JetpackNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class JetpackMovement {
    private static final double AIR_FRICTION = 0.91;
    private static final double COAST_FRICTION = 0.93;
    private static final double MIN_COAST_SPEED = 0.004;
    private static final double SPRINT_MULT = 3.4;
    private static final double WALK_MULT = 2.6;
    private static final double BOOST_RAMP_UP = 0.06;
    private static final double BOOST_RAMP_DOWN = 0.10;

    private static final java.util.Map<java.util.UUID, Double> BOOST_FACTORS = new java.util.concurrent.ConcurrentHashMap<>();

    private JetpackMovement() {}

    public static double getBoostFactor(java.util.UUID uuid) {
        return BOOST_FACTORS.getOrDefault(uuid, 0.0);
    }

    public static void clear(java.util.UUID uuid) {
        BOOST_FACTORS.remove(uuid);
    }

    public static void applyAirMobility(ServerPlayer player, ItemStack jetpackStack, JetpackItem jetpack) {
        java.util.UUID uuid = player.getUUID();
        double factor = BOOST_FACTORS.getOrDefault(uuid, 0.0);

        if (player.onGround() || player.isSpectator()) {
            if (factor > 0.0) {
                factor = Math.max(0.0, factor - BOOST_RAMP_DOWN);
                if (factor <= 0.0) BOOST_FACTORS.remove(uuid); else BOOST_FACTORS.put(uuid, factor);
            }
            return;
        }

        Input input = player.getLastClientInput();
        if (input == null) return;

        float strafe = (input.left() ? 1.0f : 0.0f) + (input.right() ? -1.0f : 0.0f);
        float forward = (input.forward() ? 1.0f : 0.0f) + (input.backward() ? -1.0f : 0.0f);

        Vec3 v = player.getDeltaMovement();
        double x = v.x;
        double z = v.z;
        boolean hasInput = Math.abs(forward) > 1.0e-3f || Math.abs(strafe) > 1.0e-3f;

        double baseSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        boolean sprinting = input.sprint() && forward > 0.0f;
        double baseMult = sprinting ? SPRINT_MULT : WALK_MULT;

        boolean wantsBoost = JetpackNetworking.isBoosting(player) && hasInput && jetpack.getJetpackTier().supportsBoost();
        double boostMaxMult = JetpackTier.getBoostSpeedMultiplier() / 100.0;

        if (wantsBoost) {
            int baseCost = jetpack.getJetpackTier().getEnergyCost();
            int boostCost = (int) Math.max(1, baseCost * (JetpackTier.getBoostEnergyMultiplier() / 100.0) - baseCost);
            int scaledCost = (int) Math.max(1, boostCost * Math.max(factor, 0.1));
            if (jetpack.tryUseEnergy(jetpackStack, scaledCost)) {
                factor = Math.min(1.0, factor + BOOST_RAMP_UP);
            } else {
                factor = Math.max(0.0, factor - BOOST_RAMP_DOWN);
            }
        } else {
            factor = Math.max(0.0, factor - BOOST_RAMP_DOWN);
        }

        if (factor <= 0.0) BOOST_FACTORS.remove(uuid); else BOOST_FACTORS.put(uuid, factor);

        double mult = baseMult * (1.0 + (boostMaxMult - 1.0) * factor);
        double maxSpeed = baseSpeed * mult;

        if (hasInput) {
            x /= AIR_FRICTION;
            z /= AIR_FRICTION;

            float yawRad = player.getYRot() * ((float) Math.PI / 180F);
            double sin = Math.sin(yawRad);
            double cos = Math.cos(yawRad);

            double wishX = strafe * cos - forward * sin;
            double wishZ = forward * cos + strafe * sin;
            double wishLen = Math.hypot(wishX, wishZ);
            wishX /= wishLen;
            wishZ /= wishLen;

            double accel = Math.max(maxSpeed * 0.35, 0.04);
            x += wishX * accel;
            z += wishZ * accel;

            double speed = Math.hypot(x, z);
            if (speed > maxSpeed) {
                x = x / speed * maxSpeed;
                z = z / speed * maxSpeed;
            }
        } else {
            x *= COAST_FRICTION;
            z *= COAST_FRICTION;

            if (Math.hypot(x, z) < MIN_COAST_SPEED) {
                x = 0.0;
                z = 0.0;
            }
        }

        if (Math.abs(x - v.x) > 1.0e-4 || Math.abs(z - v.z) > 1.0e-4) {
            player.setDeltaMovement(x, v.y, z);
            player.hurtMarked = true;
        }
    }
}
