package com.github.theredbrain.neoforge.platform;

import com.github.theredbrain.bundleapi.platform.ClientAbstraction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

public record NeoForgeClientAbstraction() implements ClientAbstraction {
    public static final NeoForgeClientAbstraction INSTANCE = new NeoForgeClientAbstraction();

    @Override
    public <T extends CustomPayload> void registerGlobalReceiverPlay(CustomPayload.Id<T> type, PlayPacketReceiver<T> receiver) {
        NeoForgeCommonAbstraction.INSTANCE.addLateAction(bus -> {
            bus.addListener(RegisterClientPayloadHandlersEvent.class, e -> {
                e.register(type, (p, ctx) -> receiver.receive(MinecraftClient.getInstance(), (ClientPlayerEntity) ctx.player(), p));
            });
        });
    }
}