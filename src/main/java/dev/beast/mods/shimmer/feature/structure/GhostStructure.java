package dev.beast.mods.shimmer.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.util.JsonCodecReloadListener;
import dev.latvian.mods.kmath.Rotation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public record GhostStructure(
	StructureRenderer structure,
	EntityFilter visibleTo,
	Vec3 pos,
	Vec3 scale,
	Rotation rotation
) {
	public static final Codec<GhostStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		StructureRenderer.GHOST_CODEC.fieldOf("structure").forGetter(GhostStructure::structure),
		EntityFilter.CODEC.optionalFieldOf("visible_to", EntityFilter.ANY.instance()).forGetter(GhostStructure::visibleTo),
		ShimmerCodecs.VEC_3.fieldOf("pos").forGetter(GhostStructure::pos),
		ShimmerCodecs.VEC_3.optionalFieldOf("scale", new Vec3(1D, 1D, 1D)).forGetter(GhostStructure::scale),
		Rotation.CODEC.optionalFieldOf("rotation", Rotation.NONE).forGetter(GhostStructure::rotation)
	).apply(instance, GhostStructure::new));

	public static List<GhostStructure> LIST = List.of();

	public static class Loader extends JsonCodecReloadListener<GhostStructure> {
		public Loader() {
			super("shimmer/ghost_structure", CODEC, true);
		}

		@Override
		protected void apply(Map<ResourceLocation, GhostStructure> from) {
			StructureRenderer.redrawAll();
			LIST = List.copyOf(from.values());
		}
	}
}
