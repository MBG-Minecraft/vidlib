package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.AAIBB;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.render.BoxRenderer;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import dev.latvian.mods.vidlib.util.FrameInfo;
import dev.latvian.mods.vidlib.util.JsonCodecReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record GhostStructure(
	StructureRenderer structure,
	EntityFilter visibleTo,
	RegistryRef<Location> location,
	Optional<AAIBB> slice,
	boolean preload,
	boolean inflate
) {
	public static final Codec<GhostStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		StructureRenderer.GHOST_CODEC.fieldOf("structure").forGetter(GhostStructure::structure),
		EntityFilter.CODEC.optionalFieldOf("visible_to", EntityFilter.ANY.instance()).forGetter(GhostStructure::visibleTo),
		Location.CLIENT_CODEC.fieldOf("location").forGetter(GhostStructure::location),
		AAIBB.CODEC.optionalFieldOf("slice").forGetter(GhostStructure::slice),
		Codec.BOOL.optionalFieldOf("preload", false).forGetter(GhostStructure::preload),
		Codec.BOOL.optionalFieldOf("inflate", false).forGetter(GhostStructure::inflate)
	).apply(instance, GhostStructure::new));

	public static List<GhostStructure> LIST = List.of();

	public static class Loader extends JsonCodecReloadListener<GhostStructure> {
		public Loader() {
			super("vidlib/ghost_structure", CODEC, true);
		}

		@Override
		protected GhostStructure finalize(GhostStructure s) {
			if (s.preload) {
				StructureStorage.CLIENT.ref(s.structure.id).get().get();
			}

			return s;
		}

		@Override
		protected void apply(Map<ResourceLocation, GhostStructure> from) {
			StructureRenderer.redrawAll();
			LIST = List.copyOf(from.values());
		}
	}

	public static void render(FrameInfo frame) {
		if (!LIST.isEmpty()) {
			var mc = frame.mc();
			var ms = frame.poseStack();

			for (var gs : LIST) {
				for (var pos : gs.location.get().positions()) {
					if (gs.slice.isPresent()) {
						var b = gs.slice.get();
						double minX = pos.x + b.minX();
						double minY = pos.y + b.minY();
						double minZ = pos.z + b.minZ();
						double maxX = pos.x + b.maxX() + 1D;
						double maxY = pos.y + b.maxY() + 1D;
						double maxZ = pos.z + b.maxZ() + 1D;

						if (!frame.isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
							continue;
						}
					}

					if (gs.visibleTo().test(mc.player)) {
						ms.pushPose();
						frame.translate(pos);

						if (gs.slice.isPresent() && frame.mc().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
							var b = gs.slice.get();
							BoxRenderer.renderDebugLines(b.minX(), b.minY(), b.minZ(), b.maxX() + 1F, b.maxY() + 1F, b.maxZ() + 1F, ms, frame.buffers(), Color.RED);
						}

						gs.structure().origin = BlockPos.containing(pos);
						gs.structure().inflate = gs.inflate();
						gs.structure().render(ms);
						ms.popPose();
					}
				}
			}
		}
	}
}
