package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.latvian.mods.kmath.AAIBB;
import dev.latvian.mods.vidlib.VidLib;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record Anchor(List<Area> areas, Map<ResourceKey<Level>, List<AAIBB>> shapes) {
	public static final Anchor NONE = new Anchor(List.of(), Map.of());
	public static Anchor client = NONE;

	public static Anchor create(List<Area> areas) {
		var map = new Reference2ObjectArrayMap<ResourceKey<Level>, List<AAIBB>>(1);

		for (var area : areas) {
			map.computeIfAbsent(area.dimension(), k -> new ArrayList<>()).add(area.shape());
		}

		return new Anchor(List.copyOf(areas), map);
	}

	public static Anchor create(ResourceKey<Level> dimension, BlockPos start, BlockPos end) {
		return create(List.of(new Area(dimension, start, end)));
	}

	public static final Codec<Anchor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Area.CODEC.listOf().optionalFieldOf("areas", List.of()).forGetter(Anchor::areas)
	).apply(instance, list -> list.isEmpty() ? NONE : create(list)));

	public static final StreamCodec<RegistryFriendlyByteBuf, Anchor> STREAM_CODEC = CompositeStreamCodec.of(
		Area.STREAM_CODEC.list(), Anchor::areas,
		list -> list.isEmpty() ? NONE : create(list)
	);

	public static final KnownCodec<Anchor> KNOWN_CODEC = KnownCodec.register(Shimmer.id("anchor"), CODEC, STREAM_CODEC, Anchor.class);
	public static final TicketController TICKET_CONTROLLER = new TicketController(VidLib.id("anchor"));
}
