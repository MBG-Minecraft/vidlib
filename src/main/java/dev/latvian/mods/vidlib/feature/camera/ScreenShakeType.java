package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import org.joml.Vector2dc;

public interface ScreenShakeType {
	SimpleRegistry<ScreenShakeType> REGISTRY = SimpleRegistry.create(ScreenShakeType::type);

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(LemniscateScreenShakeType.DEFAULT);
		REGISTRY.register(LemniscateScreenShakeType.HORIZONTAL);
		REGISTRY.register(LemniscateScreenShakeType.VERTICAL);
		REGISTRY.register(LemniscateScreenShakeType.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	Vector2dc get(float progress);
}
