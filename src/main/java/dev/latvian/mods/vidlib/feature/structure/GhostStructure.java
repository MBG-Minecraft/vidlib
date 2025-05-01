package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.Rotation;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.util.FrameInfo;
import dev.latvian.mods.vidlib.util.JsonCodecReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public record GhostStructure(
	StructureRenderer structure,
	EntityFilter visibleTo,
	Vec3 pos,
	Vec3 scale,
	Rotation rotation,
	boolean preload,
	boolean inflate
) {
	public static final Codec<GhostStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		StructureRenderer.GHOST_CODEC.fieldOf("structure").forGetter(GhostStructure::structure),
		EntityFilter.CODEC.optionalFieldOf("visible_to", EntityFilter.ANY.instance()).forGetter(GhostStructure::visibleTo),
		VLCodecs.VEC_3.fieldOf("pos").forGetter(GhostStructure::pos),
		VLCodecs.VEC_3.optionalFieldOf("scale", new Vec3(1D, 1D, 1D)).forGetter(GhostStructure::scale),
		Rotation.CODEC.optionalFieldOf("rotation", Rotation.NONE).forGetter(GhostStructure::rotation),
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
				if (gs.visibleTo().test(mc.player)) {
					ms.pushPose();
					frame.translate(gs.pos());
					ms.scale((float) gs.scale().x, (float) gs.scale().y, (float) gs.scale().z);
					ms.mulPose(Axis.YP.rotation(gs.rotation().yawRad()));
					ms.mulPose(Axis.XP.rotation(gs.rotation().pitchRad()));
					ms.mulPose(Axis.ZP.rotation(gs.rotation().rollRad()));
					gs.structure().origin = BlockPos.containing(gs.pos());
					gs.structure().inflate = gs.inflate();
					gs.structure().render(ms);
					ms.popPose();
				}
			}
		}
	}
}
