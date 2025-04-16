package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.kmath.Vec2d;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public interface CameraShakeType {
	SimpleRegistry<CameraShakeType> REGISTRY = SimpleRegistry.create(CameraShakeType::type);

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(LemniscateCameraShakeType.DEFAULT);
		REGISTRY.register(LemniscateCameraShakeType.HORIZONTAL);
		REGISTRY.register(LemniscateCameraShakeType.VERTICAL);
		REGISTRY.register(LemniscateCameraShakeType.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	Vec2d get(float progress);
}
