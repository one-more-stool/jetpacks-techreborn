package com.stool.jetpackstechreborn.items;

import com.stool.jetpackstechreborn.config.JetpackConfig;
import com.stool.jetpackstechreborn.config.JetpackConfigHandler;
import net.minecraft.world.item.equipment.ArmorMaterial;
import reborncore.common.powerSystem.RcEnergyTier;

import java.util.function.Function;

public enum JetpackTier {
    SIMPLE("simple", RcEnergyTier.MEDIUM, JetpackArmorMaterials.SIMPLE,
        c -> c.simpleCapacity, c -> c.simpleThrust / 100.0, c -> (int)c.simpleEnergyCost, c -> (int)c.simpleHoverCost),
    ADVANCED("advanced", RcEnergyTier.HIGH, JetpackArmorMaterials.ADVANCED,
        c -> c.advancedCapacity, c -> c.advancedThrust / 100.0, c -> (int)c.advancedEnergyCost, c -> (int)c.advancedHoverCost),
    INDUSTRIAL("industrial", RcEnergyTier.EXTREME, JetpackArmorMaterials.INDUSTRIAL,
        c -> c.industrialCapacity, c -> c.industrialThrust / 100.0, c -> (int)c.industrialEnergyCost, c -> (int)c.industrialHoverCost),
    ULTIMATE("ultimate", RcEnergyTier.INSANE, JetpackArmorMaterials.ULTIMATE,
        c -> c.ultimateCapacity, c -> c.ultimateThrust / 100.0, c -> (int)c.ultimateEnergyCost, c -> (int)c.ultimateHoverCost);

    public final String name;
    public final RcEnergyTier energyTier;
    public final ArmorMaterial armorMaterial;
    private final Function<JetpackConfig, Long> capacityFunc;
    private final Function<JetpackConfig, Double> thrustFunc;
    private final Function<JetpackConfig, Integer> energyCostFunc;
    private final Function<JetpackConfig, Integer> hoverCostFunc;

    JetpackTier(String name, RcEnergyTier energyTier, ArmorMaterial armorMaterial,
                Function<JetpackConfig, Long> capacityFunc, 
                Function<JetpackConfig, Double> thrustFunc, 
                Function<JetpackConfig, Integer> energyCostFunc, 
                Function<JetpackConfig, Integer> hoverCostFunc) {
        this.name = name;
        this.energyTier = energyTier;
        this.armorMaterial = armorMaterial;
        this.capacityFunc = capacityFunc;
        this.thrustFunc = thrustFunc;
        this.energyCostFunc = energyCostFunc;
        this.hoverCostFunc = hoverCostFunc;
    }

    public long getCapacity() {
        return capacityFunc.apply(JetpackConfigHandler.getConfig());
    }

    public double getThrust() {
        return thrustFunc.apply(JetpackConfigHandler.getConfig());
    }

    public double getHoverDescent() {
        return JetpackConfigHandler.getConfig().hoverDescent / 100.0;
    }

    public double getHoverActivationVelocity() {
        return JetpackConfigHandler.getConfig().hoverActivationVelocity / 100.0;
    }

    public static int getBoostSpeedMultiplier() {
        return (int) JetpackConfigHandler.getConfig().boostSpeedMultiplier;
    }

    public static int getBoostEnergyMultiplier() {
        return (int) JetpackConfigHandler.getConfig().boostEnergyMultiplier;
    }

    public int getEnergyCost() {
        return energyCostFunc.apply(JetpackConfigHandler.getConfig());
    }

    public int getHoverCost() {
        return hoverCostFunc.apply(JetpackConfigHandler.getConfig());
    }

    public boolean supportsBoost() {
        return this == INDUSTRIAL || this == ULTIMATE;
    }
}
