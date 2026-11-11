package dev.latvian.mods.vidlib.feature.platform.neoforge;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record NeoForgePacketContext(SimplePayload payload, IPayloadContext parent) implements Context {
	@Override
	@Nullable
	public MinecraftServer server() {
		return listener() instanceof ServerCommonPacketListenerImpl serverCommonListener ? serverCommonListener.server : null;
	}

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
		return parent.enqueueWork(task).exceptionally(ex -> {
			VidLib.LOGGER.error("Failed to process a synchronized task of the payload: %s".formatted(getClass().getName()), ex);
			return null;
		});
	}

	@Override
	public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
		return parent.enqueueWork(task).exceptionally(ex -> {
			VidLib.LOGGER.error("Failed to process a synchronized task of the payload: %s".formatted(getClass().getName()), ex);
			return null;
		});
	}
}
