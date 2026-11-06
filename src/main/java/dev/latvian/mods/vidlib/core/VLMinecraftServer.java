package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.session.ServerWorldSessionData;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VLMinecraftServer extends VLMinecraftEnvironment {
	default MinecraftServer vl$self() {
		return (MinecraftServer) this;
	}

	@Override
	default boolean isClient() {
		return false;
	}

	@Override
	default ServerLevel vl$level() {
		return vl$self().overworld();
	}

	@Nullable
	default ServerWorldSessionData vl$getWorldData() {
		throw new NoMixinException(this);
	}

	default void vl$setWorldData(@Nullable ServerWorldSessionData data) {
		throw new NoMixinException(this);
	}

	@Override
	default List<? extends Player> vl$getS2CPlayers() {
		return vl$self().getPlayerList().getPlayers();
	}

	@ApiStatus.Internal
	default void vl$playerJoined(ServerPlayer player) {
		PlayerProfiles.cache(player.getGameProfile());
		player.vl$sessionData().dataMap.set(InternalPlayerData.ONLINE, true);
		player.vl$sessionData().dataMap.set(InternalPlayerData.NAME, player.getScoreboardName());
		VidLib.sync(player, 2);
	}

	@ApiStatus.Internal
	default void vl$playerLeft(ServerPlayer player) {
		player.vl$sessionData().dataMap.set(InternalPlayerData.ONLINE, false);
	}

	@Override
	default void setAnchor(Anchor anchor) {
		VLMinecraftEnvironment.super.setAnchor(anchor);

		for (var level : vl$self().getAllLevels()) {
			level.vl$updateLoadedChunks();
		}
	}

	@Override
	default String getServerBrand() {
		return "neoforge";
	}

	default void runServerCommand(String command) {
		vl$self().getCommands().performPrefixedCommand(vl$self().createCommandSourceStack(), command);
	}
}
