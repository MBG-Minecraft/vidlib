package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.AAIBB;
import dev.latvian.mods.kmath.Rotation;
import dev.latvian.mods.kmath.Vec3f;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.render.BoxRenderer;
import dev.latvian.mods.kmath.render.BufferSupplier;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import dev.latvian.mods.vidlib.util.JsonCodecReloadListener;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record GhostStructure(
	List<StructureRenderer> structures,
	double animationTicks,
	EntityFilter visibleTo,
	List<RegistryRef<Location>> locations,
	Vec3f scale,
	Rotation rotation,
	Optional<AAIBB> slice,
	boolean preload,
	boolean inflate
) {
	public static final Codec<GhostStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		StructureRenderer.GHOST_CODEC.listOf().fieldOf("structures").forGetter(GhostStructure::structures),
		Codec.DOUBLE.optionalFieldOf("animation_ticks", 1D).forGetter(GhostStructure::animationTicks),
		EntityFilter.CODEC.optionalFieldOf("visible_to", EntityFilter.ANY.instance()).forGetter(GhostStructure::visibleTo),
		Location.CLIENT_CODEC.listOf().fieldOf("locations").forGetter(GhostStructure::locations),
		Vec3f.CODEC.optionalFieldOf("scale", Vec3f.ONE).forGetter(GhostStructure::scale),
		Rotation.CODEC.optionalFieldOf("rotation", Rotation.NONE).forGetter(GhostStructure::rotation),
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
				for (var st : s.structures) {
					StructureStorage.CLIENT.ref(st.id).get().get();
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

	public static void render(FrameInfo frame) {
		if (LIST.isEmpty()) {
			return;
		}

		var mc = frame.mc();
		var ms = frame.poseStack();
		var numCtx = mc.level.globalContext();

		for (var gs : LIST) {
			if (gs.structures.isEmpty() || gs.locations.isEmpty()) {
				continue;
			}

			for (var loc : gs.locations) {
				for (var wpos : loc.get().positions()) {
					var pos = wpos.get(numCtx);
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

						if (gs.slice.isPresent() && frame.mc().getEntityRenderDispatcher().shouldRenderHitBoxes() && frame.layer() == TerrainRenderLayer.TRANSLUCENT) {
							var b = gs.slice.get();
							BoxRenderer.lines(ms, b.minX(), b.minY(), b.minZ(), b.maxX() + 1F, b.maxY() + 1F, b.maxZ() + 1F, frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Color.RED);
						}

						var blockPos = BlockPos.containing(pos);
						var time = (long) ((mc.level.getGameTime() + RandomSource.create(blockPos.asLong()).nextInt(32768)) / Math.abs(gs.animationTicks));
						int index;

						if (gs.animationTicks < 0D) {
							index = RandomSource.create(time).nextInt(gs.structures.size());
						} else {
							index = (int) (time % gs.structures.size());
						}

						var s = gs.structures.get(index);

						s.origin = BlockPos.containing(pos);
						s.inflate = gs.inflate();

						ms.scale(gs.scale.x(), gs.scale.y(), gs.scale.z());
						ms.mulPose(Axis.YP.rotation(gs.rotation.yawRad()));
						ms.mulPose(Axis.XP.rotation(gs.rotation.pitchRad()));
						ms.mulPose(Axis.ZP.rotation(gs.rotation.rollRad()));
						s.render(ms, frame.layer());

						ms.popPose();
					}
				}
			}
		}
	}
}
