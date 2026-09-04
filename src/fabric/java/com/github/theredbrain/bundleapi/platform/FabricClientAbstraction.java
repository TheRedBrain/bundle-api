package com.github.theredbrain.bundleapi.platform;

import net.minecraft.network.packet.CustomPayload;

public record FabricClientAbstraction() implements ClientAbstraction {
	public static final ClientAbstraction INSTANCE = new FabricClientAbstraction();

	@Override
	public <T extends CustomPayload> void registerGlobalReceiverPlay(CustomPayload.Id<T> type, PlayPacketReceiver<T> receiver) {
		ClientPlayNetworking.registerGlobalReceiver(type, (p, ctx) -> receiver.receive(ctx.client(), ctx.player(), p));
	}
}
