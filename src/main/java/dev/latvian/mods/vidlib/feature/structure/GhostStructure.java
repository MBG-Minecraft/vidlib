package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.util.JsonCodecReloadListener;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record GhostStructure(
	boolean ghostChunks,
	List<GhostStructurePart> structures,
	Optional<StructureRendererData> data,
	double animationTicks,
	EntityFilter visibleTo,
	List<KVector> locations,
	KVector scale,
	Rotation rotation,
	boolean preload
) {
	public static final Codec<GhostStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("ghost_chunks", false).forGetter(GhostStructure::ghostChunks),
		GhostStructurePart.CODEC.listOf().fieldOf("structures").forGetter(GhostStructure::structures),
		StructureRendererData.CODEC.optionalFieldOf("data").forGetter(GhostStructure::data),
		Codec.DOUBLE.optionalFieldOf("animation_ticks", 1D).forGetter(GhostStructure::animationTicks),
		EntityFilter.CODEC.optionalFieldOf("visible_to", EntityFilter.ANY.instance()).forGetter(GhostStructure::visibleTo),
		KVector.CODEC.listOf().fieldOf("locations").forGetter(GhostStructure::locations),
		KVector.CODEC.optionalFieldOf("scale", KVector.ONE).forGetter(GhostStructure::scale),
		Rotation.CODEC.optionalFieldOf("rotation", Rotation.NONE).forGetter(GhostStructure::rotation),
		Codec.BOOL.optionalFieldOf("preload", false).forGetter(GhostStructure::preload)
	).apply(instance, GhostStructure::new));

	public static List<GhostStructure> LIST = List.of();
	private static List<VisibleGhostStructure> VISIBLE = List.of();

	private record VisibleGhostStructure(
		GhostStructure structure,
		StructureRenderer renderer,
		@Nullable AAIBB slice,
		Vec3 pos,
		BlockPos blockPos,
		Vec3f scale
	) {
	}

	public static class Loader extends JsonCodecReloadListener<GhostStructure> {
		public Loader() {
			super("vidlib/ghost_structure", CODEC, true);
		}

		@Override
		protected GhostStructure finalize(GhostStructure s) {
			if (s.preload) {
				for (var st : s.structures) {
					StructureStorage.CLIENT.ref(st.structure().id).get().get();
				}
			}

			return s;
		}

		@Override
		protected void apply(Map<ResourceLocation, GhostStructure> from) {
			StructureRenderer.redrawAll();
			LIST = List.copyOf(from.values());
		}
	}

	public static void preRender(FrameInfo frame, KNumberContext ctx) {
		// TODO: Collect visible structures here first

		var mc = frame.mc();
		var visible = new ArrayList<VisibleGhostStructure>();

		for (var gs : LIST) {
			if (gs.structures.isEmpty() || gs.locations.isEmpty()) {
				continue;
			}

			for (var loc : gs.locations) {
				var pos = loc.get(ctx);

				if (pos == null) {
					continue;
				}

				if (!gs.visibleTo().test(mc.player)) {
					continue;
				}

				var scale = gs.scale.get(ctx);

				if (scale == null) {
					continue;
				}

				var scale3f = Vec3f.of(scale);

				if (scale3f.x() == 0F || scale3f.y() == 0F || scale3f.z() == 0F) {
					continue;
				}

				var blockPos = BlockPos.containing(pos);
				var selectedStructures = gs.structures;

				if (gs.animationTicks != 0D) {
					var time = (long) ((mc.level.getGameTime() + frame.worldDelta() + RandomSource.create(blockPos.asLong()).nextInt(32768)) / Math.abs(gs.animationTicks));
					int index;

					if (gs.animationTicks < 0D) {
						index = RandomSource.create(time).nextInt(gs.structures.size());
					} else {
						index = (int) (time % gs.structures.size());
					}

					selectedStructures = List.of(gs.structures.get(index));
				}

				for (var s : selectedStructures) {
					if (s.bounds().isPresent()) {
						var b = s.bounds().get();
						double minX = pos.x + b.minX();
						double minY = pos.y + b.minY();
						double minZ = pos.z + b.minZ();
						double maxX = pos.x + b.maxX() + 1D;
						double maxY = pos.y + b.maxY() + 1D;
						double maxZ = pos.z + b.maxZ() + 1D;

						if (frame.distanceSq(minX, minY, minZ, maxX, maxY, maxZ) > 8192D * 8192D) {
							continue;
						}

						if (!frame.isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
							continue;
						}
					}

					visible.add(new VisibleGhostStructure(
						gs,
						s.structure(),
						s.bounds().orElse(null),
						pos,
						blockPos,
						scale3f
					));
				}
			}
		}

		VISIBLE = visible;
	}

	public static void render(FrameInfo frame) {
		if (VISIBLE.isEmpty()) {
			return;
		}

		var mc = frame.mc();
		var ms = frame.poseStack();

		for (var str : VISIBLE) {
			ms.pushPose();
			frame.translate(str.pos);

			if (str.slice != null && mc.getEntityRenderDispatcher().shouldRenderHitBoxes() && frame.layer() == TerrainRenderLayer.TRANSLUCENT) {
				var b = str.slice;
				CuboidRenderer.lines(ms, b.minX(), b.minY(), b.minZ(), b.maxX() + 1F, b.maxY() + 1F, b.maxZ() + 1F, frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Color.RED);
			}

			var s = str.renderer;

			s.origin = str.blockPos;
			// s.inflate = str.structure.inflate();

			ms.scale(str.scale.x(), str.scale.y(), str.scale.z());
			ms.mulPose(Axis.YP.rotation(str.structure.rotation.yawRad()));
			ms.mulPose(Axis.XP.rotation(str.structure.rotation.pitchRad()));
			ms.mulPose(Axis.ZP.rotation(str.structure.rotation.rollRad()));
			s.render(ms, frame.layer(), str.structure.data.orElse(str.structure.ghostChunks ? StructureRendererData.DEFAULT_GHOST_CHUNKS : StructureRendererData.DEFAULT));

			ms.popPose();
		}
	}
}
