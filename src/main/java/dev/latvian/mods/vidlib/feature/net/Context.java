package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.vidlib.core.VLPacketListener;
import dev.latvian.mods.vidlib.core.VLServerConfigPacketListener;
import dev.latvian.mods.vidlib.feature.session.LoginData;
import dev.latvian.mods.vidlib.feature.session.SessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Context {
	VidLibPacketPayloadContainer payload();

	PacketListener listener();

	Player player();

	void send(Packet<?> packet);

	default Level level() {
		return player().level();
	}

	default long uid() {
		return payload().uid();
	}

	default long remoteGameTime() {
		return payload().remoteGameTime();
	}

	default SessionData sessionData() {
		return ((VLPacketListener) listener()).vl$sessionData();
	}

	default CompletableFuture<Void> enqueueWork(Runnable task) {
		task.run();
		return CompletableFuture.completedFuture(null);
	}

	default <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
		return CompletableFuture.completedFuture(task.get());
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
		return ((VLPacketListener) listener()).vl$sessionData().uuid;
	}

	private UUID clientUuid() {
		return Minecraft.getInstance().getUser().getProfileId();
	}

	default void finishTask(ConfigurationTask.Type type) {
		if (listener() instanceof VLServerConfigPacketListener listener) {
			listener.vl$finishTask(type);
		} else {
			throw new UnsupportedOperationException("Attempted to complete a configuration task outside of the configuration phase!");
		}
	}

	default void addLoginData(LoginData data) {
		if (listener() instanceof VLPacketListener listener) {
			listener.vl$addLoginData(data);
		} else {
			throw new UnsupportedOperationException("Attempted to add login data outside of the configuration phase!");
		}
	}

	default boolean isConfig() {
		return listener() instanceof ServerConfigurationPacketListener || listener() instanceof ClientConfigurationPacketListener;
	}
}
