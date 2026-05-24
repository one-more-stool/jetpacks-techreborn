package com.stool.jetpackstechreborn.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JetpackJumpPayload(boolean jumping) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JetpackJumpPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("jetpacks", "jump"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JetpackJumpPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, JetpackJumpPayload::jumping,
        JetpackJumpPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
