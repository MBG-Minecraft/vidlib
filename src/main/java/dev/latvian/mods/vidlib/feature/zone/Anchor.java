package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.AAIBB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record Anchor(List<AAIBB> volumes) {
	public static final Anchor NONE = new Anchor(List.of());

	public static Anchor create(List<AAIBB> volumes) {
		return volumes.isEmpty() ? NONE : new Anchor(volumes);
	}

	public static Anchor create(BlockPos start, BlockPos end) {
		return create(List.of(new AAIBB(start, end)));
	}

	public static final Codec<Anchor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		AAIBB.CODEC.listOf().optionalFieldOf("volumes", List.of()).forGetter(Anchor::volumes)
	).apply(instance, Anchor::create));

	public static final StreamCodec<RegistryFriendlyByteBuf, Anchor> STREAM_CODEC = CompositeStreamCodec.of(
		KLibStreamCodecs.listOf(AAIBB.STREAM_CODEC), Anchor::volumes,
		Anchor::create
	);

	public static final DataType<Anchor> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC);
}
