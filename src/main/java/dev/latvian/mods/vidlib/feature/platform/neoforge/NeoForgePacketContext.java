package dev.latvian.mods.vidlib.feature.platform.neoforge;

import dev.latvian.mods.vidlib.VidLib;
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
		if (listener().getMainThreadEventLoop().isSameThread()) {
			task.run();
			return CompletableFuture.completedFuture(null);
		}

		return listener().getMainThreadEventLoop().submit(task).exceptionally(ex -> {
			VidLib.LOGGER.error("Failed to process a synchronized task of the payload: %s".formatted(getClass().getName()), ex);
			return null;
		});
	}

	@Override
	public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
		if (listener().getMainThreadEventLoop().isSameThread()) {
			return CompletableFuture.completedFuture(task.get());
		}

		return listener().getMainThreadEventLoop().submit(task).exceptionally(ex -> {
			VidLib.LOGGER.error("Failed to process a synchronized task of the payload: %s".formatted(getClass().getName()), ex);
			return null;
		});
	}
}
