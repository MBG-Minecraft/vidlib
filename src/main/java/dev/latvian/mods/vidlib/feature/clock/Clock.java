package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.util.JsonRegistryReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Clock(
	ResourceLocation id,
	List<ClockLocation> locations,
	Optional<ScreenClock> screen
) {
	public static final Codec<Clock> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(Clock::id),
		ClockLocation.CODEC.listOf().optionalFieldOf("locations", List.of()).forGetter(Clock::locations),
		ScreenClock.CODEC.optionalFieldOf("screen").forGetter(Clock::screen)
	).apply(instance, Clock::new));

	public static final VLRegistry<Clock> REGISTRY = VLRegistry.createClient("clock", Clock.class);

	public static final Color RED = Color.of(1F, 1F, 0.3F, 0.3F);

	public static class Loader extends JsonRegistryReloadListener<Clock> {
		public Loader() {
			super("vidlib/clock", DIRECT_CODEC, true, REGISTRY);
		}

		@Override
		protected void apply(ResourceManager resourceManager, Map<ResourceLocation, Clock> map) {
			super.apply(resourceManager, map);
			ClockCommands.CLOCK_IDS.clear();
			ClockCommands.CLOCK_IDS.addAll(map.keySet());
		}
	}
}
