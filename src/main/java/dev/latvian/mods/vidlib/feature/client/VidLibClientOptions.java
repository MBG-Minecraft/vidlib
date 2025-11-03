package dev.latvian.mods.vidlib.feature.client;

import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;

import java.util.Arrays;

public interface VidLibClientOptions {
	OptionInstance<Boolean> ADMIN_PANEL = OptionInstance.createBoolean("options.vidlib.admin_panel", false);
	OptionInstance<Boolean> SHOW_FPS = OptionInstance.createBoolean("options.vidlib.show_fps", false);
	OptionInstance<Boolean> SHOW_ANCHOR = OptionInstance.createBoolean("options.vidlib.show_anchor", false);
	OptionInstance<Boolean> SHOW_ZONES = OptionInstance.createBoolean("options.vidlib.show_zones", false);
	OptionInstance<Boolean> SHOW_ZONE_OUTER_BOUNDS = OptionInstance.createBoolean("options.vidlib.show_zone_outer_bounds", true);
	OptionInstance<Boolean> SHOW_COORDINATES = OptionInstance.createBoolean("options.vidlib.show_coordinates", false);
	OptionInstance<Boolean> LOCK_GUI_SCALE = OptionInstance.createBoolean("options.vidlib.lock_gui_scale", true, v -> Minecraft.getInstance().resizeDisplay());

	OptionInstance<ZoneRenderType> ZONE_RENDER_TYPE = new OptionInstance<>(
		"options.vidlib.zone_render_type",
		OptionInstance.noTooltip(),
		OptionInstance.forOptionEnum(),
		new OptionInstance.Enum<>(Arrays.asList(ZoneRenderType.values()), ZoneRenderType.DATA_TYPE.codec()),
		ZoneRenderType.NORMAL,
		callback -> {
		}
	);

	OptionInstance<BlockFilter> ZONE_BLOCK_FILTER = UnconfigurableValueSet.create(
		"options.vidlib.zone_block_filter",
		BlockFilter.DATA_TYPE.codec(),
		OptionInstance.noTooltip(),
		BlockFilter.ANY.instance(),
		callback -> {
			var mc = Minecraft.getInstance();

			if (mc.player != null) {
				mc.player.vl$sessionData().refreshBlockZones();
			}
		}
	);

	// OptionInstance<ExplosionData> TEST_EXPLOSION = DataKey.PLAYER.buildDefault("options.vidlib.test_explosion", ExplosionData.DATA_TYPE, ExplosionData.DEFAULT);

	OptionInstance<PhysicsParticleData> TEST_PHYSICS_PARTICLE_DATA = UnconfigurableValueSet.create(
		"options.vidlib.test_physics_particles.data",
		PhysicsParticleData.DATA_TYPE.codec(),
		OptionInstance.noTooltip(),
		PhysicsParticleData.DEFAULT,
		callback -> {
			var mc = Minecraft.getInstance();

			if (mc.player != null) {
				mc.player.vl$sessionData().refreshBlockZones();
			}
		}
	);

	OptionInstance<Double> TEST_SCREEN_SHAKE_MAX_DISTANCE = new OptionInstance<>(
		"options.vidlib.test_screen_shake.max_distance",
		OptionInstance.noTooltip(),
		(l, v) -> Options.genericValueLabel(l, v.intValue()),
		new OptionInstance.IntRange(0, 1000).xmap(i -> (double) i, Double::intValue),
		Codec.doubleRange(0.0, 1000.0),
		30.0,
		callback -> {
		}
	);

	OptionInstance<?>[] CONTROLS_OPTIONS = {
		ADMIN_PANEL,
	};

	OptionInstance<?>[] ACCESSIBILITY_OPTIONS = {
		SHOW_ANCHOR,
		SHOW_FPS,
		SHOW_ZONES,
		SHOW_ZONE_OUTER_BOUNDS,
		ZONE_RENDER_TYPE,
		TEST_SCREEN_SHAKE_MAX_DISTANCE,
		SHOW_COORDINATES,
		LOCK_GUI_SCALE,
	};

	static boolean getAdminPanel() {
		return ADMIN_PANEL.get();
	}

	static boolean getShowFPS() {
		return SHOW_FPS.get();
	}

	static void setShowFPS(boolean value) {
		SHOW_FPS.set(value);
		Minecraft.getInstance().options.save();
	}

	static boolean getShowAnchor() {
		return SHOW_ANCHOR.get();
	}

	static boolean getShowZones() {
		return SHOW_ZONES.get();
	}

	static boolean getShowZoneOuterBounds() {
		return SHOW_ZONE_OUTER_BOUNDS.get();
	}

	static ZoneRenderType getZoneRenderType() {
		return ZONE_RENDER_TYPE.get();
	}

	static BlockFilter getZoneBlockFilter() {
		return ZONE_BLOCK_FILTER.get();
	}

	static boolean getShowCoordinates() {
		return SHOW_COORDINATES.get();
	}

	static void setShowCoordinates(boolean value) {
		SHOW_COORDINATES.set(value);
		Minecraft.getInstance().options.save();
	}

	static void process(Options.FieldAccess accessor) {
		accessor.process("vidlib.admin_panel", ADMIN_PANEL);

		accessor.process("vidlib.show_fps", SHOW_FPS);
		accessor.process("vidlib.show_anchor", SHOW_ANCHOR);
		accessor.process("vidlib.show_zones", SHOW_ZONES);
		accessor.process("vidlib.show_zone_outer_bounds", SHOW_ZONE_OUTER_BOUNDS);
		accessor.process("vidlib.zone_render_type", ZONE_RENDER_TYPE);
		accessor.process("vidlib.zone_block_filter", ZONE_BLOCK_FILTER);
		accessor.process("vidlib.show_coordinates", SHOW_COORDINATES);
		accessor.process("vidlib.lock_gui_scale", LOCK_GUI_SCALE);
	}
}
