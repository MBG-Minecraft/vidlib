package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.volume.UniverseVolume;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class LevelSessionDataEnvironmentAttributeSystem extends EnvironmentAttributeSystem {
	public final EnvironmentAttributeSystem fallback;
	public final LevelSessionData<?> sessionData;

	public LevelSessionDataEnvironmentAttributeSystem(EnvironmentAttributeSystem fallback, LevelSessionData<?> sessionData) {
		super(Map.of());
		this.fallback = fallback;
		this.sessionData = sessionData;
	}

	@Override
	public <Value> Value getDimensionValue(EnvironmentAttribute<Value> attribute) {
		var value = fallback.getDimensionValue(attribute);

		for (var zone : sessionData.zones) {
			if (zone.volume == UniverseVolume.INSTANCE) {
				var attr = zone.atmosphere.get(attribute);

				if (attr != null) {
					value = attr.applyModifier(value);
				}
			}
		}

		var attr = sessionData.getAtmosphere().get(attribute);

		if (attr != null) {
			value = attr.applyModifier(value);
		}

		return value;
	}

	@Override
	public <Value> Value getValue(EnvironmentAttribute<Value> attribute, Vec3 pos, @Nullable SpatialAttributeInterpolator biomeInterpolator) {
		var value = fallback.getValue(attribute, pos, biomeInterpolator);

		for (var zone : sessionData.zones) {
			if (zone.volume.contains(pos)) {
				var attr = zone.atmosphere.get(attribute);

				if (attr != null) {
					value = attr.applyModifier(value);
				}
			}
		}

		var attr = sessionData.getAtmosphere().get(attribute);

		if (attr != null) {
			value = attr.applyModifier(value);
		}

		return value;
	}
}
