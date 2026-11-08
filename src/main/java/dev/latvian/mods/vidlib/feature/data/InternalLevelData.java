package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.platform.PlatformType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.atmosphere.Atmosphere;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.environment.FluidPlane;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.waypoint.Waypoint;
import dev.latvian.mods.vidlib.feature.zone.Anchor;

import java.util.List;

public interface InternalLevelData {
	DataKey<LevelSessionData<?>, Ref<Atmosphere>> ATMOSPHERE = DataKey.LEVEL.createDefault("atmosphere", Atmosphere.DATA_TYPE, null);
	DataKey<LevelSessionData<?>, Anchor> ANCHOR = DataKey.LEVEL.createDefault("anchor", Anchor.DATA_TYPE, Anchor.NONE);
	DataKey<LevelSessionData<?>, List<Waypoint>> WAYPOINTS = DataKey.LEVEL.createDefault("waypoints", Waypoint.LIST_DATA_TYPE, List.of());
	DataKey<LevelSessionData<?>, FluidPlane> FLUID_PLANE = DataKey.LEVEL.createDefault("fluid_plane", FluidPlane.DATA_TYPE, null);
	DataKey<LevelSessionData<?>, Boolean> ICE_MELTS = DataKey.LEVEL.createBoolean("ice_melts", true);
	DataKey<LevelSessionData<?>, Boolean> BLOCK_GRAVITY = DataKey.LEVEL.createBoolean("block_gravity", true);
	DataKey<LevelSessionData<?>, Boolean> SMALL_GRASS_HITBOX = DataKey.LEVEL.createBoolean("small_grass_hitbox", PlatformHelper.INSTANCE.getPlatform() == PlatformType.NEOFORGE);
	DataKey<LevelSessionData<?>, Boolean> SOFT_BARRIERS = DataKey.LEVEL.createBoolean("soft_barriers", PlatformHelper.INSTANCE.getPlatform() == PlatformType.NEOFORGE);

	@AutoInit
	static void bootstrap() {
	}
}
