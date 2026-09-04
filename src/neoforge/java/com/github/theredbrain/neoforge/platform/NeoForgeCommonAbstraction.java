package com.github.theredbrain.neoforge.platform;

import com.github.theredbrain.bundleapi.platform.CommonAbstraction;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record NeoForgeCommonAbstraction(List<Consumer<IEventBus>> lateActions) implements CommonAbstraction {
	public static IEventBus EVENT_BUS = null;
	public static final NeoForgeCommonAbstraction INSTANCE = new NeoForgeCommonAbstraction(new ArrayList<>());

	@Override
	public <T extends CustomPayload> void registerClientboundPlayPayload(CustomPayload.Id<T> type, PacketCodec<PacketByteBuf, T> codec) {
		addLateAction(bus -> bus.addListener(RegisterPayloadHandlersEvent.class, e -> {
			e.registrar("1").playToClient(type, codec);
		}));
	}

	@Override
	public <T extends CustomPayload> void registerServerboundPlayPayload(CustomPayload.Id<T> type, PacketCodec<PacketByteBuf, T> codec, PlayPacketReceiver<T> receiver) {
		addLateAction(bus -> bus.addListener(RegisterPayloadHandlersEvent.class, e -> {
			e.registrar("1").playToServer(type, codec, (payload, context) -> {
				receiver.receive((ServerPlayerEntity) context.player(), payload);
			});
		}));
	}

	@Override
	public boolean isClient() {
		return FMLEnvironment.getDist().isClient();
	}

	public void addLateAction(Consumer<IEventBus> consumer) {
		if (EVENT_BUS != null) {
			consumer.accept(EVENT_BUS);
		} else {
			this.lateActions.add(consumer);
		}
	}
}
