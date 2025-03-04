package dev.beast.mods.shimmer.feature.clock;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.util.JsonCodecReloadListener;
import dev.beast.mods.shimmer.util.ShimmerCodecs;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public record Clock(
	ResourceLocation id,
	ResourceKey<Level> dimension,
	int maxTicks,
	boolean countDown,
	int flash,
	Map<String, IntList> events,
	List<ClockLocation> locations
) {
	public static final Codec<Clock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(Clock::id),
		ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Clock::dimension),
		Codec.INT.optionalFieldOf("max_ticks", 12000).forGetter(Clock::maxTicks),
		Codec.BOOL.optionalFieldOf("count_down", true).forGetter(Clock::countDown),
		Codec.INT.optionalFieldOf("flash", 200).forGetter(Clock::flash),
		Codec.unboundedMap(Codec.STRING, ShimmerCodecs.INT_LIST_OR_SELF).optionalFieldOf("events", Map.of()).forGetter(Clock::events),
		ClockLocation.CODEC.listOf().optionalFieldOf("locations", List.of()).forGetter(Clock::locations)
	).apply(instance, Clock::new));

	public static final StreamCodec<ByteBuf, Clock> STREAM_CODEC = ShimmerStreamCodecs.composite(
		ResourceLocation.STREAM_CODEC,
		Clock::id,
		ShimmerStreamCodecs.optional(ShimmerStreamCodecs.DIMENSION, Level.OVERWORLD),
		Clock::dimension,
		ByteBufCodecs.VAR_INT,
		Clock::maxTicks,
		ByteBufCodecs.BOOL,
		Clock::countDown,
		ByteBufCodecs.VAR_INT,
		Clock::flash,
		ShimmerStreamCodecs.unboundedMap(ByteBufCodecs.STRING_UTF8, ShimmerStreamCodecs.VAR_INT_LIST),
		Clock::events,
		ClockLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
		Clock::locations,
		Clock::new
	);

	public static Map<ResourceLocation, Clock> SERVER = Map.of();
	public static Map<ResourceLocation, ClockInstance> SERVER_INSTANCES = Map.of();

	public static int forCommand(CommandContext<CommandSourceStack> ctx, BiConsumer<ClockInstance, MinecraftServer> callback) {
		var instance = Clock.SERVER_INSTANCES.get(ResourceLocationArgument.getId(ctx, "id"));

		if (instance != null) {
			callback.accept(instance, ctx.getSource().getServer());
			return 1;
		}

		return 0;
	}

	public static class Loader extends JsonCodecReloadListener<Clock> {
		public Loader() {
			super("shimmer/clock", CODEC, true);
		}

		@Override
		protected void apply(Map<ResourceLocation, Clock> from) {
			var newInstances = new HashMap<ResourceLocation, ClockInstance>();

			SERVER = Map.copyOf(from);

			for (var entry : from.entrySet()) {
				var instance = new ClockInstance(entry.getValue(), 0, false);
				var old = SERVER_INSTANCES.get(entry.getKey());

				if (old != null) {
					instance.prevTick = old.prevTick;
					instance.tick = old.tick;
					instance.ticking = old.ticking;
				}

				newInstances.put(entry.getKey(), instance);
			}

			SERVER_INSTANCES = newInstances;
		}
	}
}
