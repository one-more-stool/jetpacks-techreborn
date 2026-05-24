package com.stool.jetpackstechreborn.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JetpackBoostPayload(boolean boosting) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JetpackBoostPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("jetpacks", "boost"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JetpackBoostPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, JetpackBoostPayload::boosting,
        JetpackBoostPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
