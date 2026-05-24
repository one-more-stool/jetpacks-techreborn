package com.stool.jetpackstechreborn.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JetpackTogglePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JetpackTogglePayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("jetpacks", "toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JetpackTogglePayload> CODEC = StreamCodec.unit(new JetpackTogglePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
