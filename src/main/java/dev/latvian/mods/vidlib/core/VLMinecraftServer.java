package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VLMinecraftServer extends VLMinecraftEnvironment {
	default MinecraftServer vl$self() {
		return (MinecraftServer) this;
	}

	@Override
	default boolean isClientEnvironment() {
		return false;
	}

	@Override
	default ServerLevel vl$level() {
		return vl$self().overworld();
	}

	@Nullable
	default ServerSessionData vl$getGlobalData() {
		throw new NoMixinException(this);
	}

	@Override
	default List<? extends Player> vl$getS2CPlayers() {
		return vl$self().getPlayerList().getPlayers();
	}

	@Override
	default String getServerBrand() {
		return "neoforge";
	}

	default void runServerCommand(String command) {
		vl$self().getCommands().performPrefixedCommand(vl$self().createCommandSourceStack(), command);
	}
}
