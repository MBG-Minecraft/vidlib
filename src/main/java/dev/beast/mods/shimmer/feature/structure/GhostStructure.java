package dev.beast.mods.shimmer.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.util.JsonCodecReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public record GhostStructure(
	StructureRenderer structure,
	EntityFilter visibleTo,
	Vec3 pos,
	Vec3 scale,
	Vec3 rotation
) {
	public static final Codec<GhostStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		StructureRenderer.GHOST_CODEC.fieldOf("structure").forGetter(GhostStructure::structure),
		EntityFilter.CODEC.optionalFieldOf("visible_to", EntityFilter.ALL.instance()).forGetter(GhostStructure::visibleTo),
		Vec3.CODEC.fieldOf("pos").forGetter(GhostStructure::pos),
		Vec3.CODEC.optionalFieldOf("scale", new Vec3(1D, 1D, 1D)).forGetter(GhostStructure::scale),
		Vec3.CODEC.optionalFieldOf("rotation", Vec3.ZERO).forGetter(GhostStructure::rotation)
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
