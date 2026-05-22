package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryEntry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import org.joml.Vector2dc;

public interface ScreenShakeType extends SimpleRegistryEntry {
	SimpleRegistry<ScreenShakeType> REGISTRY = SimpleRegistry.create(VidLib.id("screen_shake_type"), c -> PlatformHelper.CURRENT.collectScreenShakeTypes(c));

	static void builtinTypes(SimpleRegistryCollector<ScreenShakeType> registry) {
		registry.register(LemniscateScreenShakeType.DEFAULT);
		registry.register(LemniscateScreenShakeType.HORIZONTAL);
		registry.register(LemniscateScreenShakeType.VERTICAL);
		registry.register(LemniscateScreenShakeType.TYPE);
	}

	@Override
	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	Vector2dc get(float progress);
}
