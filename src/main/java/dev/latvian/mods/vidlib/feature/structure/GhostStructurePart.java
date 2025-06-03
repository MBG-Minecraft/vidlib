package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.AAIBB;

import java.util.Optional;
import java.util.function.Function;

public record GhostStructurePart(
	StructureRenderer structure,
	Optional<AAIBB> bounds
) {
	public static final Codec<GhostStructurePart> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		StructureRenderer.GHOST_CODEC.fieldOf("structure").forGetter(GhostStructurePart::structure),
		AAIBB.CODEC.optionalFieldOf("bounds").forGetter(GhostStructurePart::bounds)
	).apply(instance, GhostStructurePart::new));

	public static final Codec<GhostStructurePart> CODEC = Codec.either(DIRECT_CODEC, StructureRenderer.GHOST_CODEC).xmap(
		e -> e.map(Function.identity(), s -> new GhostStructurePart(s, Optional.empty())),
		s -> s.bounds.isPresent() ? Either.left(s) : Either.right(s.structure)
	);
}
