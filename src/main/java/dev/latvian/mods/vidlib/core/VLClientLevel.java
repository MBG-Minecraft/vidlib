package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.zone.ActiveZones;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.stream.Stream;

public interface VLClientLevel extends VLLevel {
	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return Minecraft.getInstance();
	}

	@Override
	default ClientLevel vl$level() {
		return (ClientLevel) this;
	}

	@Override
	default ClientProps getProps() {
		throw new NoMixinException(this);
	}

	@Override
	@Nullable
	default ActiveZones vl$getActiveZones() {
		var player = Minecraft.getInstance().player;
		return player == null ? null : player.vl$sessionData().filteredZones;
	}

	@Override
	default boolean isReplayLevel() {
		var mc = Minecraft.getInstance();
		return mc.player != null && mc.player.isReplayCamera();
	}

	@Override
	default Iterable<Entity> allEntities() {
		return vl$level().entitiesForRendering();
	}

	@Override
	default float vl$getDelta() {
		return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
	}

	@Override
	default void vl$setDayTime(long time) {
		vl$level().getLevelData().setDayTime(time);
	}

	@Override
	default Stream<LevelChunk> vl$getChunks() {
		var chunks = vl$level().getChunkSource().storage.chunks;
		int len = chunks.length();
		var list = new ArrayList<LevelChunk>(len);

		for (int i = 0; i < len; i++) {
			var chunk = chunks.get(i);

			if (chunk != null && !chunk.isEmpty()) {
				list.add(chunk);
			}
		}

		return list.stream();
	}
}
