package com.stool.jetpackstechreborn.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JetpackHoverTogglePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JetpackHoverTogglePayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("jetpacks", "hover_toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JetpackHoverTogglePayload> CODEC = StreamCodec.unit(new JetpackHoverTogglePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
