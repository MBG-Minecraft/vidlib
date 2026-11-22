package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.klib.util.GameUtils;
import dev.latvian.mods.vidlib.core.api.client.VLClientPacketListener;
import dev.latvian.mods.vidlib.feature.VLFeatures;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Context {
	@Nullable
	default MinecraftServer server() {
		return listener() instanceof ServerCommonPacketListenerImpl lc ? GameUtils.getServer(lc) : null;
	}

	SimplePayload payload();

	PacketListener listener();

	Player player();

	void send(Packet<?> packet);

	default Level level() {
		return player().level();
	}

	default GlobalSessionData globalSessionData() {
		return switch (listener()) {
			case VLClientPacketListener listener -> listener.vl$getGlobalData();
			case ServerCommonPacketListenerImpl lc -> ServerSessionData.of(GameUtils.getServer(lc));
			case null, default -> throw new IllegalStateException("Unknown listener type: " + listener());
		};
	}

	default LevelSessionData<?> levelSessionData() {
		if (listener() instanceof ServerConfigurationPacketListenerImpl lc) {
			return ServerSessionData.of(GameUtils.getServer(lc)).getLevel(Level.OVERWORLD);
		}

		return LevelSessionData.of(level());
	}

	default PlayerSessionData playerSessionData() {
		if (listener() instanceof ServerConfigurationPacketListenerImpl lc) {
			return ServerSessionData.of(GameUtils.getServer(lc)).getPlayer(lc.getOwner().id());
		}

		return PlayerSessionData.of(player());
	}

	default VLFeatures features() {
		return (VLFeatures) globalSessionData();
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
		if (listener() instanceof ServerConfigurationPacketListenerImpl listener) {
			listener.finishCurrentTask(type);
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
