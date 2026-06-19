package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface PropRenderer<P extends Prop> {
	Set<Object> DEFAULT_STAGES = Set.of(
		SubmitCustomGeometryEvent.class
	);

	Set<Object> TERRAIN_STAGES = Set.of(
		dev.latvian.mods.vidlib.util.TerrainRenderLayer.SOLID,
		dev.latvian.mods.vidlib.util.TerrainRenderLayer.CUTOUT_MIPPED,
		dev.latvian.mods.vidlib.util.TerrainRenderLayer.CUTOUT,
		dev.latvian.mods.vidlib.util.TerrainRenderLayer.TRANSLUCENT,
		dev.latvian.mods.vidlib.util.TerrainRenderLayer.TRIPWIRE
	);

	Set<Object> SOLID_TERRAIN_STAGES = Set.of(dev.latvian.mods.vidlib.util.TerrainRenderLayer.SOLID);
	Set<Object> CUTOUT_TERRAIN_STAGES = Set.of(dev.latvian.mods.vidlib.util.TerrainRenderLayer.CUTOUT);
	Set<Object> TRANSLUCENT_TERRAIN_STAGES = Set.of(dev.latvian.mods.vidlib.util.TerrainRenderLayer.TRANSLUCENT);

	PropRenderer<?> INVISIBLE = new PropRenderer<>() {
		@Override
		public void render(PropRenderContext<Prop> ctx) {
		}

		@Override
	public Set<Object> getStages(Prop prop) {
		return Set.of();
	}
	};

	static Holder holder(PropType<?> type, PropRenderer<?> unitRenderer) {
		return new Holder(type, prop -> unitRenderer, unitRenderer);
	}

	static Holder holder(PropType<?> type, Function<Prop, PropRenderer<?>> rendererFactory) {
		return new Holder(type, rendererFactory, null);
	}

	record Holder(PropType<?> type, Function<Prop, PropRenderer<?>> rendererFactory, @Nullable PropRenderer<?> unit) {
	}

	Lazy<Map<PropType<?>, Holder>> ALL = Lazy.identityMap(map -> {
		for (var s : ClientAutoRegister.SCANNED.get()) {
			if (s.value() instanceof Holder h) {
				map.put(h.type, h);
			}
		}
	});

	default void setup(Minecraft mc, FrameGraphSetupEvent event) {
	}

	void render(PropRenderContext<P> ctx);

	default Set<Object> getStages(P prop) {
		return DEFAULT_STAGES;
	}

	default int getPackedLight(PropRenderContext<P> ctx) {
		return LightCoordsUtil.FULL_BRIGHT;
	}

	default boolean shouldSort(PropRenderContext<P> ctx) {
		return false;
	}
}
