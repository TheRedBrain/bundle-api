package com.github.theredbrain.bundleapi.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record FabricCommonAbstraction() implements CommonAbstraction {
	public static final CommonAbstraction INSTANCE = new FabricCommonAbstraction();

	@Override
	public <T extends CustomPayload> void registerServerboundPlayPayload(CustomPayload.Id<T> type, PacketCodec<PacketByteBuf, T> codec, PlayPacketReceiver<T> receiver) {
		PayloadTypeRegistry.playC2S().register(type, codec);
		ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> receiver.receive(context.player(), payload));
	}

	@Override
	public <T extends CustomPayload> void registerClientboundPlayPayload(CustomPayload.Id<T> type, PacketCodec<PacketByteBuf, T> codec) {
		PayloadTypeRegistry.playS2C().register(type, codec);
	}

	@Override
	public boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}
}
