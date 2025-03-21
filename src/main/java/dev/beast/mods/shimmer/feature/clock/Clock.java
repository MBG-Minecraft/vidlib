package dev.beast.mods.shimmer.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.JsonCodecReloadListener;
import dev.beast.mods.shimmer.util.registry.RegistryReference;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Clock(
	ResourceLocation id,
	ResourceKey<Level> dimension,
	int maxTicks,
	boolean countDown,
	int flash,
	Map<String, IntList> events,
	List<ClockLocation> locations
) {
	public static final Codec<Clock> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(Clock::id),
		ShimmerCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Clock::dimension),
		Codec.INT.optionalFieldOf("max_ticks", 12000).forGetter(Clock::maxTicks),
		Codec.BOOL.optionalFieldOf("count_down", true).forGetter(Clock::countDown),
		Codec.INT.optionalFieldOf("flash", 200).forGetter(Clock::flash),
		Codec.unboundedMap(Codec.STRING, ShimmerCodecs.INT_LIST_OR_SELF).optionalFieldOf("events", Map.of()).forGetter(Clock::events),
		ClockLocation.CODEC.listOf().optionalFieldOf("locations", List.of()).forGetter(Clock::locations)
	).apply(instance, Clock::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Clock> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
		ResourceLocation.STREAM_CODEC, Clock::id,
		ShimmerStreamCodecs.DIMENSION.optional(Level.OVERWORLD), Clock::dimension,
		ByteBufCodecs.VAR_INT, Clock::maxTicks,
		ByteBufCodecs.BOOL, Clock::countDown,
		ByteBufCodecs.VAR_INT, Clock::flash,
		ByteBufCodecs.STRING_UTF8.unboundedMap(ShimmerStreamCodecs.VAR_INT_LIST), Clock::events,
		ClockLocation.STREAM_CODEC.list(), Clock::locations,
		Clock::new
	);

	public static final RegistryReference.IdHolder<Clock> REGISTRY = RegistryReference.createServerIdHolder("clock", false);
	public static final KnownCodec<Clock> KNOWN_CODEC = KnownCodec.register(REGISTRY, Clock.class);

	public static class Loader extends JsonCodecReloadListener<Clock> {
		public Loader() {
			super("shimmer/clock", DIRECT_CODEC, true);
		}

		@Override
		protected void apply(Map<ResourceLocation, Clock> from) {
			var newInstances = new HashMap<ResourceLocation, ClockInstance>();

			REGISTRY.update(Map.copyOf(from));

			for (var entry : from.entrySet()) {
				var instance = new ClockInstance(entry.getValue(), 0, false);
				var old = ClockInstance.REGISTRY.get(entry.getKey());

				if (old != null) {
					instance.prevTick = old.prevTick;
					instance.tick = old.tick;
					instance.ticking = old.ticking;
				}

				newInstances.put(entry.getKey(), instance);
			}

			ClockInstance.REGISTRY.update(newInstances);
		}
	}
}
