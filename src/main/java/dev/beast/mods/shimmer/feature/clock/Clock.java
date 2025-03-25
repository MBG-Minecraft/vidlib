package dev.beast.mods.shimmer.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.JsonRegistryReloadListener;
import dev.beast.mods.shimmer.util.registry.RegistryReference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
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

	public static final StreamCodec<RegistryFriendlyByteBuf, Clock> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
		ResourceLocation.STREAM_CODEC, Clock::id,
		ClockLocation.STREAM_CODEC.list(), Clock::locations,
		ScreenClock.STREAM_CODEC.optional(), Clock::screen,
		Clock::new
	);

	public static final RegistryReference.IdHolder<Clock> REGISTRY = RegistryReference.createServerIdHolder("clock", false);
	public static final KnownCodec<Clock> KNOWN_CODEC = KnownCodec.of(REGISTRY, Clock.class);

	public static final Color RED = Color.of(1F, 1F, 0.3F, 0.3F);

	public static class Loader extends JsonRegistryReloadListener<Clock> {
		public Loader() {
			super("shimmer/clock", DIRECT_CODEC, true, REGISTRY);
		}
	}
}
