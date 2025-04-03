package dev.beast.mods.shimmer.feature.camera;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.math.Vec2d;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;

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
