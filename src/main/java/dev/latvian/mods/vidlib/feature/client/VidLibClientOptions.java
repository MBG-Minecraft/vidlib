package dev.latvian.mods.vidlib.feature.client;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.util.client.UnconfigurableValueSet;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

public interface VidLibClientOptions {
	OptionInstance<Boolean> SHOW_ANCHOR = OptionInstance.createBoolean("options.vidlib.show_anchor", false);
	OptionInstance<Boolean> SHOW_ZONES = OptionInstance.createBoolean("options.vidlib.show_zones", false);
	OptionInstance<Boolean> SHOW_ZONE_WALLS = OptionInstance.createBoolean("options.vidlib.show_zone_walls", true);
	OptionInstance<Boolean> SHOW_ZONE_OUTER_BOUNDS = OptionInstance.createBoolean("options.vidlib.show_zone_outer_bounds", true);
	OptionInstance<Boolean> SHOW_COORDINATES = OptionInstance.createBoolean("options.vidlib.show_coordinates", false);
	OptionInstance<Boolean> LOCK_IMGUI_SCROLL = OptionInstance.createBoolean("options.vidlib.lock_imgui_scroll", false);

	OptionInstance<ZoneRenderType> ZONE_RENDER_TYPE = new OptionInstance<>(
		"options.vidlib.zone_render_type",
		OptionInstance.noTooltip(),
		(prefix, value) -> Options.genericValueLabel(prefix, Component.translatable(value.getKey())),
		new OptionInstance.Enum<>(Arrays.asList(ZoneRenderType.values()), ZoneRenderType.DATA_TYPE.codec()),
		ZoneRenderType.NORMAL,
		callback -> {
		}
	);

	// OptionInstance<ExplosionData> TEST_EXPLOSION = DataKey.PLAYER.buildDefault("options.vidlib.test_explosion", ExplosionData.DATA_TYPE, ExplosionData.DEFAULT);

	OptionInstance<PhysicsParticleData> TEST_PHYSICS_PARTICLE_DATA = UnconfigurableValueSet.create(
		"options.vidlib.test_physics_particles.data",
		PhysicsParticleData.DIRECT_CODEC,
		OptionInstance.noTooltip(),
		PhysicsParticleData.DEFAULT_VALUE,
		callback -> {
		}
	);

	OptionInstance<Double> TEST_SCREEN_SHAKE_MAX_DISTANCE = new OptionInstance<>(
		"options.vidlib.test_screen_shake.max_distance",
		OptionInstance.noTooltip(),
		(l, v) -> Options.genericValueLabel(l, v.intValue()),
		new OptionInstance.IntRange(0, 1000).xmap(i -> (double) i, Double::intValue, true),
		Codec.doubleRange(0.0, 1000.0),
		30.0,
		callback -> {
		}
	);

	OptionInstance<?>[] CONTROLS_OPTIONS = {
	};

	OptionInstance<?>[] ACCESSIBILITY_OPTIONS = {
		SHOW_ANCHOR,
		SHOW_ZONES,
		SHOW_ZONE_WALLS,
		SHOW_ZONE_OUTER_BOUNDS,
		ZONE_RENDER_TYPE,
		TEST_SCREEN_SHAKE_MAX_DISTANCE,
		SHOW_COORDINATES,
		LOCK_IMGUI_SCROLL,
	};

	static boolean getShowAnchor() {
		return SHOW_ANCHOR.get();
	}

	static boolean getShowZones() {
		return SHOW_ZONES.get();
	}

	static boolean getShowZoneWalls() {
		return SHOW_ZONE_WALLS.get();
	}

	static boolean getShowZoneOuterBounds() {
		return SHOW_ZONE_OUTER_BOUNDS.get();
	}

	static ZoneRenderType getZoneRenderType() {
		return ZONE_RENDER_TYPE.get();
	}

	static boolean getShowCoordinates() {
		return SHOW_COORDINATES.get();
	}

	static void setShowCoordinates(boolean value) {
		SHOW_COORDINATES.set(value);
		Minecraft.getInstance().options.save();
	}

	static void process(Options.FieldAccess accessor) {
		accessor.process("vidlib.show_anchor", SHOW_ANCHOR);
		accessor.process("vidlib.show_zones", SHOW_ZONES);
		accessor.process("vidlib.show_zone_walls", SHOW_ZONE_WALLS);
		accessor.process("vidlib.show_zone_outer_bounds", SHOW_ZONE_OUTER_BOUNDS);
		accessor.process("vidlib.zone_render_type", ZONE_RENDER_TYPE);
		accessor.process("vidlib.show_coordinates", SHOW_COORDINATES);
		accessor.process("vidlib.lock_imgui_scroll", LOCK_IMGUI_SCROLL);
	}
}
