package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.vidlib.feature.VLFeatures;
import dev.latvian.mods.vidlib.core.VLServerConfigPacketListener;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Context {
	SimplePayload payload();

	PacketListener listener();

	Player player();

	void send(Packet<?> packet);

	default Level level() {
		return player().level();
	}

	default VLFeatures features() {
		return (VLFeatures) globalSessionData();
	}

	default GlobalSessionData globalSessionData() {
		return levelSessionData().globalData;
	}

	default LevelSessionData<?> levelSessionData() {
		return LevelSessionData.of(level());
	}

	default PlayerSessionData playerSessionData() {
		return PlayerSessionData.of(player());
	}

	default CompletableFuture<Void> enqueueWork(Runnable task) {
		task.run();
		return CompletableFuture.completedFuture(null);
	}

	default <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
		return CompletableFuture.completedFuture(task.get());
	}

	default boolean isAdmin() {
		return level().getServer().isSingleplayer() || player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	default boolean isReplay() {
		return globalSessionData().isReplay;
	}

	default void finishTask(ConfigurationTask.Type type) {
		if (listener() instanceof VLServerConfigPacketListener listener) {
			listener.vl$finishTask(type);
		} else {
			throw new UnsupportedOperationException("Attempted to complete a configuration task outside of the configuration phase!");
		}
	}

	default boolean isConfig() {
		return listener() instanceof ServerConfigurationPacketListener || listener() instanceof ClientConfigurationPacketListener;
	}

	default LevelSessionData vl() {
		return LevelSessionData.of(level());
	}
}
