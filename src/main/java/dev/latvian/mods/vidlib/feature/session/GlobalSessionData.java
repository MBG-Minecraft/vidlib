package dev.latvian.mods.vidlib.feature.session;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.entity.PlayerInput;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.net.S2CPacketConsumer;
import dev.latvian.mods.klib.util.LevelGameTimeProvider;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.InternalGlobalData;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.note.Note;
import dev.latvian.mods.vidlib.util.NameDrawType;
import dev.latvian.mods.vidlib.util.RepeatingTask;
import dev.latvian.mods.vidlib.util.ScheduledTask;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;

public abstract class GlobalSessionData implements LevelGameTimeProvider {
	public final Map<ResourceKey<Level>, LevelSessionData<?>> levelMap;
	public final Map<UUID, PlayerSessionData> playerMap;
	public final KNumberVariables globalVariables;
	public final ScheduledTask.Handler scheduledTaskHandler;
	public final DataMap<GlobalSessionData> dataMap;
	public RegistryAccess registryAccess;
	public DynamicOps<JsonElement> jsonOps;
	public DynamicOps<Tag> nbtOps;
	public TagParser<Tag> nbtParser;
	public boolean isReplay;
	public final Map<String, ParticleOptions> particleCache;

	public GlobalSessionData(RegistryAccess registryAccess) {
		this.levelMap = new Reference2ObjectOpenHashMap<>();
		this.playerMap = new Object2ObjectOpenHashMap<>();
		this.globalVariables = new KNumberVariables();
		this.scheduledTaskHandler = new ScheduledTask.Handler(this);
		this.dataMap = new DataMap<>(this, this, DataKey.GLOBAL);
		updateOps(registryAccess);
		this.isReplay = false;
		this.particleCache = new Object2ObjectOpenHashMap<>();
	}

	public abstract PauseType getPauseType();

	public abstract FeatureSet getServerFeatures();

	public LevelSessionData<?> getLevel(ResourceKey<Level> dimension) {
		var data = levelMap.get(dimension);

		if (data == null) {
			data = createLevelData(dimension);
			levelMap.put(dimension, data);
		}

		return data;
	}

	protected abstract LevelSessionData<?> createLevelData(ResourceKey<Level> dimension);

	public PlayerSessionData getPlayer(UUID uuid) {
		return playerMap.computeIfAbsent(uuid, this::createPlayerData);
	}

	protected abstract PlayerSessionData createPlayerData(UUID uuid);

	public void updateOps(RegistryAccess r) {
		registryAccess = r;
		jsonOps = registryAccess.createSerializationContext(JsonOps.INSTANCE);
		nbtOps = registryAccess.createSerializationContext(NbtOps.INSTANCE);
		nbtParser = TagParser.create(nbtOps);
	}

	public void preTick(PauseType pauseType) {
	}

	public void postTick(PauseType pauseType) {
		if (pauseType.tick()) {
			scheduledTaskHandler.tick();
		}
	}

	public void schedule(int delay, Runnable task) {
		scheduledTaskHandler.run(delay, new RepeatingTask.WrappedRunnable(task));
	}

	public void scheduleRepeating(int delay, RepeatingTask task) {
		scheduledTaskHandler.run(delay, task);
	}

	public void removePlayer(UUID uuid) {
		playerMap.remove(uuid);
	}

	public void updateInput(Level level, UUID player, PlayerInput input) {
	}

	public void updateAtmospheres() {
	}

	public void setClientModListSentDuringConfig() {
	}

	public void createNote(Note note) {
	}

	public void deleteNote(UUID id) {
	}

	public void setEntityBrightnessOverride(int entityId, int override) {
	}

	public ParticleOptions getParticleOptions(String string) {
		return particleCache.computeIfAbsent(string, str -> {
			try {
				return ParticleArgument.readParticle(new StringReader(string), registryAccess);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to parse particle from '" + str + "'", ex);
				return DustParticleOptions.REDSTONE;
			}
		});
	}

	public void sync(S2CPacketConsumer packets) {
		dataMap.syncAll(packets, SyncServerDataPayload::new);
	}

	public NameDrawType getNameDrawType() {
		return dataMap.get(InternalGlobalData.NAME_DRAW_TYPE);
	}

	public void setNameDrawType(NameDrawType type) {
		dataMap.set(InternalGlobalData.NAME_DRAW_TYPE, type);
	}

	public void setNameDrawDistance(double min, double mid, double max) {
		dataMap.set(InternalGlobalData.NAME_DRAW_MIN_DIST, min);
		dataMap.set(InternalGlobalData.NAME_DRAW_MID_DIST, mid);
		dataMap.set(InternalGlobalData.NAME_DRAW_MAX_DIST, max);
	}
}
