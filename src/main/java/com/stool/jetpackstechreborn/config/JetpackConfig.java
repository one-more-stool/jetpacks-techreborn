package com.stool.jetpackstechreborn.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class JetpackConfig {
    @SerialEntry public long simpleCapacity = 100_000;
    @SerialEntry public long simpleThrust = 15; // as integer percentage 0.15 * 100
    @SerialEntry public long simpleEnergyCost = 10;
    @SerialEntry public long simpleHoverCost = 5;

    @SerialEntry public long advancedCapacity = 1_000_000;
    @SerialEntry public long advancedThrust = 25;
    @SerialEntry public long advancedEnergyCost = 20;
    @SerialEntry public long advancedHoverCost = 10;

    @SerialEntry public long industrialCapacity = 10_000_000;
    @SerialEntry public long industrialThrust = 40;
    @SerialEntry public long industrialEnergyCost = 50;
    @SerialEntry public long industrialHoverCost = 20;

    @SerialEntry public long ultimateCapacity = 100_000_000;
    @SerialEntry public long ultimateThrust = 60;
    @SerialEntry public long ultimateEnergyCost = 100;
    @SerialEntry public long ultimateHoverCost = 50;

    @SerialEntry public long hoverDescent = 5; // 0.05 * 100
    // hover engages when vertical velocity falls to this value or below (0 = at apex)
    @SerialEntry public long hoverActivationVelocity = 0;
    // ctrl boost horizontal speed multiplier (%); 200 = 2x walk speed
    @SerialEntry public long boostSpeedMultiplier = 220;
    // Extra energy cost multiplier when boosting (%); 300 = 3x base cost
    @SerialEntry public long boostEnergyMultiplier = 300;
}
