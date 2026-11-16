package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.entity.filter.EntityFilter;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.util.JsonCodecReloadListener;
import dev.latvian.mods.vidlib.feature.client.VLWorldRenderState;
import dev.latvian.mods.vidlib.feature.prop.builtin.structure.StructureProp;
import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record GhostStructure(
	boolean ghostChunks,
	List<GhostStructurePart> structures,
	Optional<StructureRendererData> data,
	double animationTicks,
	Ref<EntityFilter> visibleTo,
	List<Ref<KVector>> locations,
	Ref<KVector> scale,
	Ref<KVector> rotation,
	boolean preload,
	boolean preloadRenderer,
	boolean solid
) {
	public static final ImBoolean VISIBLE_CONFIG = new ImBoolean(true);

	public static final Codec<GhostStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("ghost_chunks", false).forGetter(GhostStructure::ghostChunks),
		GhostStructurePart.CODEC.listOf().fieldOf("structures").forGetter(GhostStructure::structures),
		StructureRendererData.CODEC.optionalFieldOf("data").forGetter(GhostStructure::data),
		Codec.DOUBLE.optionalFieldOf("animation_ticks", 1D).forGetter(GhostStructure::animationTicks),
		EntityFilter.CODEC.optionalFieldOf("visible_to", EntityFilter.ANY).forGetter(GhostStructure::visibleTo),
		KVector.CODEC.listOf().fieldOf("locations").forGetter(GhostStructure::locations),
		KVector.CODEC.optionalFieldOf("scale", KVector.ONE).forGetter(GhostStructure::scale),
		KVector.CODEC.optionalFieldOf("rotation", KVector.ZERO).forGetter(GhostStructure::rotation),
		Codec.BOOL.optionalFieldOf("preload", false).forGetter(GhostStructure::preload),
		Codec.BOOL.optionalFieldOf("preload_renderer", false).forGetter(GhostStructure::preloadRenderer),
		Codec.BOOL.optionalFieldOf("solid", false).forGetter(GhostStructure::solid)
	).apply(instance, GhostStructure::new));

	public static List<GhostStructure> LIST = List.of();
	private static List<VisibleGhostStructure> VISIBLE = List.of();

	private static void preloadAll() {
		var mc = Minecraft.getInstance();

		if (mc.level == null) {
			return;
		}

		mc.execute(() -> {
			for (var ghostStructure : LIST) {
				if (!ghostStructure.preload && !ghostStructure.preloadRenderer) {
					continue;
				}

				var data = ghostStructure.rendererData();

				for (var part : ghostStructure.structures) {
					if (ghostStructure.preload) {
						part.structure().getStructure();
					}

					if (ghostStructure.preloadRenderer) {
						part.structure().preRender(data);
					}
				}
			}
		});
	}

	public static void preRender(VLWorldRenderState vl) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var numberContext = vl.numberContext;
		var visible = new ArrayList<VisibleGhostStructure>();

		for (var ghostStructure : LIST) {
			if (ghostStructure.structures.isEmpty() || ghostStructure.locations.isEmpty()) {
				continue;
			}

			if (!ghostStructure.visibleTo.value().test(mc.player)) {
				continue;
			}

			for (var location : ghostStructure.locations) {
				var pos = location.value().get(numberContext);

				if (pos == null) {
					continue;
				}

				var scale = ghostStructure.scale.value().get(numberContext);

				if (scale == null) {
					continue;
				}

				var scale3f = Vec3f.of(scale);

				if (scale3f.x() == 0F || scale3f.y() == 0F || scale3f.z() == 0F) {
					continue;
				}

				var rotation = ghostStructure.rotation.value().get(numberContext);
				var rotation3f = rotation == null ? Vec3f.ZERO : Vec3f.of(rotation);
				var blockPos = BlockPos.containing(pos);
				var selectedStructures = selectStructures(ghostStructure, blockPos, vl.worldDelta);

				for (var part : selectedStructures) {
					if (part.bounds().isPresent()) {
						var b = part.bounds().get();
						var box = new AABB(
							pos.x + b.minX(),
							pos.y + b.minY(),
							pos.z + b.minZ(),
							pos.x + b.maxX() + 1D,
							pos.y + b.maxY() + 1D,
							pos.z + b.maxZ() + 1D
						);

						if (box.distanceToSqr(vl.cameraPos) > 8192D * 8192D) {
							continue;
						}

						if (!vl.frustum.isVisible(box)) {
							continue;
						}
					}

					visible.add(new VisibleGhostStructure(ghostStructure, part.structure(), part.bounds().orElse(null), pos, blockPos, scale3f, rotation3f));
				}
			}
		}

		VISIBLE = visible;

		var solidPlacements = collectSolidPlacements(mc, numberContext, vl.worldDelta);
		appendStructurePropPlacements(mc, solidPlacements);
		GhostStructureCollision.update(mc.level, solidPlacements);
	}

	private static void appendStructurePropPlacements(Minecraft mc, List<GhostStructureCollision.Placement> placements) {
		if (!(mc.level instanceof ClientLevel clientLevel)) {
			return;
		}

		for (var prop : ClientLevelSessionData.of(clientLevel).props.levelProps) {
			if (!(prop instanceof StructureProp structureProp) || !structureProp.solid || structureProp.isRemoved()) {
				continue;
			}

			var holder = structureProp.getHolder();

			if (holder == null) {
				continue;
			}

			placements.add(new GhostStructureCollision.Placement(
				holder,
				null,
				structureProp.pos.x, structureProp.pos.y, structureProp.pos.z,
				1F, 1F, 1F,
				(float) Math.toRadians(structureProp.rotation.x),
				(float) Math.toRadians(-structureProp.rotation.y),
				(float) Math.toRadians(structureProp.rotation.z),
				structureProp.centerX ? -holder.size().getX() / 2D : 0D,
				structureProp.centerY ? -holder.size().getY() / 2D : 0D,
				structureProp.centerZ ? -holder.size().getZ() / 2D : 0D
			));
		}
	}

	private static List<GhostStructurePart> selectStructures(GhostStructure ghostStructure, BlockPos blockPos, float worldDelta) {
		if (ghostStructure.animationTicks == 0D) {
			return ghostStructure.structures;
		}

		long time = (long) ((Minecraft.getInstance().level.getGameTime() + worldDelta + RandomSource.create(blockPos.asLong()).nextInt(32768)) / Math.abs(ghostStructure.animationTicks));
		int index = ghostStructure.animationTicks < 0D
			? RandomSource.create(time).nextInt(ghostStructure.structures.size())
			: (int) (time % ghostStructure.structures.size());
		return List.of(ghostStructure.structures.get(index));
	}

	private static List<GhostStructureCollision.Placement> collectSolidPlacements(Minecraft mc, KNumberContext numberContext, float worldDelta) {
		var placements = new ArrayList<GhostStructureCollision.Placement>();

		for (var ghostStructure : LIST) {
			if (!ghostStructure.solid() || ghostStructure.structures.isEmpty() || ghostStructure.locations.isEmpty()) {
				continue;
			}

			if (!ghostStructure.visibleTo.value().test(mc.player)) {
				continue;
			}

			var data = ghostStructure.rendererData();

			for (var location : ghostStructure.locations) {
				var pos = location.value().get(numberContext);

				if (pos == null) {
					continue;
				}

				var scale = ghostStructure.scale.value().get(numberContext);

				if (scale == null || scale.x == 0D || scale.y == 0D || scale.z == 0D) {
					continue;
				}

				var rotation = ghostStructure.rotation.value().get(numberContext);
				var rot = rotation == null ? Vec3.ZERO : rotation;
				var blockPos = BlockPos.containing(pos);

				for (var part : selectStructures(ghostStructure, blockPos, worldDelta)) {
					var holder = part.structure().getStructure();

					if (holder == null || holder.empty()) {
						continue;
					}

					double centerX = data.centerX() ? -holder.size().getX() / 2D : 0D;
					double centerY = data.centerY() ? -holder.size().getY() / 2D : 0D;
					double centerZ = data.centerZ() ? -holder.size().getZ() / 2D : 0D;

					placements.add(new GhostStructureCollision.Placement(
						holder,
						part.bounds().orElse(null),
						pos.x, pos.y, pos.z,
						(float) scale.x, (float) scale.y, (float) scale.z,
						(float) rot.x, (float) rot.y, (float) rot.z,
						centerX, centerY, centerZ
					));
				}
			}
		}

		return placements;
	}

	public static void submit(SubmitContext ctx) {
		if (VISIBLE.isEmpty() || !VISIBLE_CONFIG.get()) {
			return;
		}

		for (var structure : VISIBLE) {
			ctx.push(structure.pos);

			if (structure.scale != Vec3f.ONE) {
				ctx.poseStack().scale(structure.scale.x(), structure.scale.y(), structure.scale.z());
			}

			if (structure.rotation != Vec3f.ZERO) {
				ctx.rotate(new Quaternionf().rotationY(structure.rotation.y()).rotateX(structure.rotation.x()).rotateZ(structure.rotation.z()));
			}

			if (structure.slice != null && Minecraft.getInstance().gui.hud.getDebugOverlay().showDebugScreen()) {
				var b = structure.slice;
				ctx.submit(RenderTypes.lines(), (pose, buffer) -> {
					var callback = pose.transform(buffer).withColor(Color.RED).withLineWidth(ctx.lineWidth());
					callback.frameLines(
						(float) b.minX(), (float) b.minY(), (float) b.minZ(),
						(float) (b.maxX() + 1D), (float) (b.maxY() + 1D), (float) (b.maxZ() + 1D),
						1F, 1F
					);
				});
			}

			var renderer = structure.renderer;
			renderer.origin = structure.blockPos;
			renderer.submit(ctx, structure.structure.rendererData());
			ctx.pop();
		}
	}

	public StructureRendererData rendererData() {
		return data.orElse(ghostChunks ? StructureRendererData.DEFAULT_GHOST_CHUNKS : StructureRendererData.DEFAULT);
	}

	private record VisibleGhostStructure(
		GhostStructure structure,
		StructureRenderer renderer,
		@Nullable AAIBB slice,
		Vec3 pos,
		BlockPos blockPos,
		Vector3fc scale,
		Vector3fc rotation
	) {
	}

	public static class Loader extends JsonCodecReloadListener<GhostStructure> {
		public Loader() {
			super("vidlib/ghost_structure", CODEC);
		}

		@Override
		protected void apply(ResourceManager resourceManager, Map<Identifier, GhostStructure> from) {
			StructureRenderer.redrawAll();
			LIST = List.copyOf(from.values());
			preloadAll();
		}
	}
}