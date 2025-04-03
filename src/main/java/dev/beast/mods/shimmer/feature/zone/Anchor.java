package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.math.AAIBB;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record Anchor(List<Area> areas, Map<ResourceKey<Level>, List<AAIBB>> shapes) {
	public record Area(ResourceKey<Level> dimension, AAIBB shape) {
		public static final Codec<Area> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ShimmerCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Area::dimension),
			AAIBB.CODEC.fieldOf("areas").forGetter(Area::shape)
		).apply(instance, Area::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Area> STREAM_CODEC = CompositeStreamCodec.of(
			ShimmerStreamCodecs.DIMENSION.optional(Level.OVERWORLD), Area::dimension,
			AAIBB.STREAM_CODEC, Area::shape,
			Area::new
		);
	}

	public static final Anchor NONE = new Anchor(List.of(), Map.of());
	public static Anchor client = NONE;

	public static Anchor create(List<Area> areas) {
		var map = new Reference2ObjectArrayMap<ResourceKey<Level>, List<AAIBB>>(1);

		for (var area : areas) {
			map.computeIfAbsent(area.dimension, k -> new ArrayList<>()).add(area.shape);
		}

		return new Anchor(List.copyOf(areas), map);
	}

	public static Anchor create(ResourceKey<Level> dimension, BlockPos start, BlockPos end) {
		return create(List.of(new Area(dimension, new AAIBB(start, end))));
	}

	public static final Codec<Anchor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Area.CODEC.listOf().optionalFieldOf("areas", List.of()).forGetter(Anchor::areas)
	).apply(instance, list -> list.isEmpty() ? NONE : create(list)));

	public static final StreamCodec<RegistryFriendlyByteBuf, Anchor> STREAM_CODEC = CompositeStreamCodec.of(
		Area.STREAM_CODEC.list(), Anchor::areas,
		list -> list.isEmpty() ? NONE : create(list)
	);

	public static final KnownCodec<Anchor> KNOWN_CODEC = KnownCodec.register(Shimmer.id("anchor"), CODEC, STREAM_CODEC, Anchor.class);
	public static final TicketType<ChunkPos> TICKET_TYPE = TicketType.create("shimmer:anchor", Comparator.comparingLong(ChunkPos::toLong));
}
