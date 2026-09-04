package com.github.theredbrain.bundleapi.platform;

import dev.yumi.mc.core.api.YumiMods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;

public interface CommonAbstraction {
	boolean IS_FABRIC = YumiMods.get().isModLoaded("fabricloader") && !YumiMods.get().isModLoaded("connector");

	CommonAbstraction INSTANCE = Util.make(() -> {
		try {
			return (CommonAbstraction) Class.forName(
					"eu.pb4.trinkets.impl.platform." +
							(CommonAbstraction.IS_FABRIC ? "fabric.FabricCommonAbstraction" : "neo.NeoCommonAbstraction")).getField("INSTANCE").get(null);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	});

	<T extends CustomPayload> void registerServerboundPlayPayload(CustomPayload.Id<T> type, PacketCodec<PacketByteBuf, T> codec, PlayPacketReceiver<T> receiver);

	<T extends CustomPayload> void registerClientboundPlayPayload(CustomPayload.Id<T> type, PacketCodec<PacketByteBuf, T> codec);

	boolean isClient();

	interface PlayPacketReceiver<T> {
		void receive(ServerPlayerEntity player, T payload);
	}
}
