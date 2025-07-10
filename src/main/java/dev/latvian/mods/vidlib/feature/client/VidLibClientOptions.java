package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
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

	OptionInstance<ZoneRenderType> ZONE_RENDER_TYPE = new OptionInstance<>(
		"options.vidlib.zone_render_type",
		OptionInstance.noTooltip(),
		OptionInstance.forOptionEnum(),
		new OptionInstance.Enum<>(Arrays.asList(ZoneRenderType.values()), ZoneRenderType.DATA_TYPE.codec()),
		ZoneRenderType.NORMAL,
		type -> {
		}
	);

	OptionInstance<BlockFilter> ZONE_BLOCK_FILTER = UnconfigurableValueSet.create(
		"options.vidlib.zone_block_filter",
		BlockFilter.DATA_TYPE.codec(),
		OptionInstance.noTooltip(),
		BlockFilter.ANY.instance(),
		type -> {
			var mc = Minecraft.getInstance();

			if (mc.player != null) {
				mc.player.vl$sessionData().refreshBlockZones();
			}
		}
	);

	// OptionInstance<ExplosionData> TEST_EXPLOSION = DataKey.PLAYER.buildDefault("options.vidlib.test_explosion", ExplosionData.DATA_TYPE, ExplosionData.DEFAULT);
	// OptionInstance<PhysicsParticleData> TEST_PARTICLES = DataKey.PLAYER.buildDefault("options.vidlib.test_physics_particles", PhysicsParticleData.DATA_TYPE, PhysicsParticleData.DEFAULT);
	// OptionInstance<Double> TEST_SCREEN_SHAKE = DataKey.PLAYER.buildDefault("options.vidlib.test_screen_shake", DataTypes.DOUBLE, 30D);

	OptionInstance<?>[] CONTROLS_OPTIONS = {
		ADMIN_PANEL,
	};

	OptionInstance<?>[] ACCESSIBILITY_OPTIONS = {
		SHOW_ANCHOR,
		SHOW_FPS,
		SHOW_ZONES,
		ZONE_RENDER_TYPE,
	};

	static boolean getAdminPanel() {
		return ADMIN_PANEL.get();
	}

	static boolean getShowFPS() {
		return SHOW_FPS.get();
	}

	static boolean getShowAnchor() {
		return SHOW_ANCHOR.get();
	}

	static boolean getShowZones() {
		return SHOW_ZONES.get();
	}

	static ZoneRenderType getZoneRenderType() {
		return ZONE_RENDER_TYPE.get();
	}

	static BlockFilter getZoneBlockFilter() {
		return ZONE_BLOCK_FILTER.get();
	}

	static void process(Options.FieldAccess accessor) {
		accessor.process("vidlib.admin_panel", ADMIN_PANEL);

		accessor.process("vidlib.show_fps", SHOW_FPS);
		accessor.process("vidlib.show_anchor", SHOW_ANCHOR);
		accessor.process("vidlib.show_zones", SHOW_ZONES);
		accessor.process("vidlib.zone_render_type", ZONE_RENDER_TYPE);
		accessor.process("vidlib.zone_block_filter", ZONE_BLOCK_FILTER);
	}
}
