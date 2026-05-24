package com.stool.jetpackstechreborn.component;

import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public class JetpackDataComponentTypes {
    public static final DataComponentType<Boolean> HOVER_MODE =
        DataComponentType.<Boolean>builder().persistent(PrimitiveCodec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();

    public static void init() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath("jetpacks", "hover_mode"), HOVER_MODE);
    }
}
