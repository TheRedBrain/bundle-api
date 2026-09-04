package com.github.theredbrain.bundleapi.platform;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Util;

public interface ClientAbstraction {
	ClientAbstraction INSTANCE = Util.make(() -> {
		try {
			return (ClientAbstraction) Class.forName(
					"com.github.theredbrain.bundleapi.platform." +
							(CommonAbstraction.IS_FABRIC ? "fabric.FabricClientAbstraction" : "neo.NeoClientAbstraction")).getField("INSTANCE").get(null);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	});

	<T extends CustomPayload> void registerGlobalReceiverPlay(CustomPayload.Id<T> type, PlayPacketReceiver<T> receiver);

	interface PlayPacketReceiver<T> {
		void receive(MinecraftClient minecraft, ClientPlayerEntity player, T payload);
	}
}
