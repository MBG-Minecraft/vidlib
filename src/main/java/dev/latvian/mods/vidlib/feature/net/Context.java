package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.session.LoginData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Context {
	VidLibPacketPayloadContainer payload();

	ICommonPacketListener listener();

	Player player();

	default Level level() {
		return player().level();
	}

	default long uid() {
		return payload().uid();
	}

	default long remoteGameTime() {
		return payload().remoteGameTime();
	}

	default CompletableFuture<Void> enqueueWork(Runnable task) {
		if (listener().getMainThreadEventLoop().isSameThread()) {
			task.run();
			return CompletableFuture.completedFuture(null);
		}

		return listener().getMainThreadEventLoop().submit(task).exceptionally(ex -> {
			VidLib.LOGGER.error("Failed to process a synchronized task of the payload: %s".formatted(getClass().getName()), ex);
			return null;
		});
	}

	default <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
		if (listener().getMainThreadEventLoop().isSameThread()) {
			return CompletableFuture.completedFuture(task.get());
		}

		return listener().getMainThreadEventLoop().submit(task).exceptionally(ex -> {
			VidLib.LOGGER.error("Failed to process a synchronized task of the payload: %s".formatted(getClass().getName()), ex);
			return null;
		});
	}

	default RandomSource createRandom() {
		return new XoroshiroRandomSource(uid(), remoteGameTime());
	}

	default long getSeed() {
		return Long.rotateLeft(uid() + remoteGameTime(), 17) + uid();
	}

	default boolean isAdmin() {
		return level().getServer().isSingleplayer() || player().hasPermissions(2);
	}

	default boolean isReplay() {
		return level().isReplayLevel();
	}

	default UUID uuid() {
		if (listener() instanceof ServerCommonPacketListenerImpl listener) {
			return listener.getOwner().getId();
		} else {
			return clientUuid();
		}
	}

	private UUID clientUuid() {
		return Minecraft.getInstance().getUser().getProfileId();
	}

	void finishTask(ConfigurationTask.Type type);

	void addLoginData(LoginData data);
}
