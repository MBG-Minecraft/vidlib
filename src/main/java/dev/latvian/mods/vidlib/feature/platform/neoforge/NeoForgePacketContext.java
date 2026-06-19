package dev.latvian.mods.vidlib.feature.platform.neoforge;

import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketPayloadContainer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record NeoForgePacketContext(VidLibPacketPayloadContainer payload, IPayloadContext parent) implements Context {
	@Override
	public ICommonPacketListener listener() {
		return parent.listener();
	}

	@Override
	public Player player() {
		return parent.player();
	}

	@Override
	public void send(Packet<?> packet) {
		parent.listener().send(packet);
	}

	@Override
	public CompletableFuture<Void> enqueueWork(Runnable task) {
		return parent.enqueueWork(task);
	}

	@Override
	public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
		return parent.enqueueWork(task);
	}
}
