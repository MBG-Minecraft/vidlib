package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.core.ShimmerBlockInWorld;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.zone.ZoneRenderType;
import dev.beast.mods.shimmer.feature.zone.shape.RotatedBoxZoneShape;
import dev.beast.mods.shimmer.feature.zone.shape.SphereZoneShape;
import dev.beast.mods.shimmer.feature.zone.shape.UniverseZoneShape;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShape;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShapeGroup;
import dev.beast.mods.shimmer.util.Cast;
import dev.beast.mods.shimmer.util.FrameInfo;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import dev.latvian.mods.kmath.SpherePoints;
import dev.latvian.mods.kmath.VoxelShapeBox;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.render.BoxRenderer;
import dev.latvian.mods.kmath.render.SphereRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

import java.util.IdentityHashMap;
import java.util.Map;

public interface ZoneRenderer<T extends ZoneShape> {
	Map<SimpleRegistryType<?>, ZoneRenderer<?>> RENDERERS = new IdentityHashMap<>();

	record Context(FrameInfo frame, Color color, Color outlineColor) {
		public MultiBufferSource buffers() {
			return frame.buffers();
		}
	}

	static void register(SimpleRegistryType<?> type, ZoneRenderer<?> renderer) {
		RENDERERS.put(type, renderer);
	}

	@AutoInit(AutoInit.Type.CLIENT_LOADED)
	static void bootstrap() {
		ZoneRenderer.register(UniverseZoneShape.TYPE, EmptyZoneRenderer.INSTANCE);
		ZoneRenderer.register(ZoneShapeGroup.TYPE, new GroupZoneRenderer());
		ZoneRenderer.register(SphereZoneShape.TYPE, new SphereZoneRenderer());
		ZoneRenderer.register(RotatedBoxZoneShape.TYPE, new RotatedBoxZoneRenderer());
	}

	static ZoneRenderer<?> get(SimpleRegistryType<?> type) {
		var renderer = RENDERERS.get(type);
		return renderer == null ? BoxZoneRenderer.INSTANCE : renderer;
	}

	static void renderAll(FrameInfo frame) {
		var mc = frame.mc();
		var ms = frame.poseStack();
		var cameraPos = frame.camera().getPosition();
		var frustum = frame.frustum();
		var renderType = mc.player.getZoneRenderType();
		var buffers = frame.buffers();
		var session = frame.session();
		var clip = session.zoneClip;

		if (clip != null && clip.pos() != null) {
			ms.pushPose();
			frame.translate(clip.pos());
			ms.scale(0.25F, 0.25F, 0.25F);
			SphereRenderer.renderDebugLines(SpherePoints.L, ms, buffers, Color.BLACK);
			ms.popPose();
		}

		if (renderType == ZoneRenderType.COLLISIONS) {
			for (var sz : session.filteredZones.getSolidZones()) {
				if (sz.instance().zone.shape().closestDistanceTo(cameraPos) <= 2000D && frustum.isVisible(sz.instance().zone.shape().getBoundingBox())) {
					boolean hovered = clip != null && clip.instance() == sz.instance();
					var baseColor = sz.instance().zone.color().withAlpha(50);
					var outlineColor = hovered ? Color.WHITE : sz.instance().entities.isEmpty() ? sz.instance().zone.color() : Color.GREEN;
					BoxRenderer.renderVoxelShape(ms, buffers, sz.shapeBox(), cameraPos.reverse(), false, baseColor, outlineColor);
				}
			}
		} else {
			for (var container : session.filteredZones) {
				for (var instance : container.zones) {
					if (instance.zone.shape().closestDistanceTo(cameraPos) <= 2000D && frustum.isVisible(instance.zone.shape().getBoundingBox())) {
						var renderer = ZoneRenderer.get(instance.zone.shape().type());

						if (renderer != EmptyZoneRenderer.INSTANCE) {
							boolean hovered = clip != null && clip.instance() == instance;
							var baseColor = instance.zone.color().withAlpha(50);
							var outlineColor = hovered ? Color.WHITE : instance.entities.isEmpty() ? instance.zone.color() : Color.GREEN;

							if (renderType == ZoneRenderType.NORMAL) {
								renderer.render(Cast.to(instance.zone.shape()), new ZoneRenderer.Context(frame, baseColor, outlineColor));
							} else if (renderType == ZoneRenderType.BLOCKS) {
								if (session.cachedZoneShapes == null) {
									session.cachedZoneShapes = new IdentityHashMap<>();
								}

								var voxelShape = session.cachedZoneShapes.get(instance.zone.shape());

								if (voxelShape == null) {
									voxelShape = VoxelShapeBox.EMPTY;
									session.cachedZoneShapes.put(instance.zone.shape(), voxelShape);

									Thread.startVirtualThread(() -> {
										var filter = mc.player.getZoneBlockFilter();

										session.cachedZoneShapes.put(instance.zone.shape(), VoxelShapeBox.of(instance.zone.shape().createBlockRenderingShape(pos -> {
											var state = mc.level.getBlockState(pos);

											if (state.isAir()) {
												return false;
											}

											return filter == BlockFilter.ANY.instance() || filter.test(ShimmerBlockInWorld.of(mc.level, pos, state));
										}).optimize()));
									});
								}

								BoxRenderer.renderVoxelShape(ms, buffers, voxelShape, cameraPos.reverse(), true, baseColor, outlineColor);
							}
						}
					}
				}
			}
		}
	}

	static void renderSolid(FrameInfo frame) {
		for (var sz : frame.session().filteredZones.getSolidZones()) {
			var zone = sz.instance().zone;
			double dist = zone.shape().closestDistanceTo(frame.camera().getPosition());

			if (dist <= 10D && zone.color().alpha() > 0 && frame.frustum().isVisible(zone.shape().getBoundingBox()) && zone.solid().test(frame.mc().player)) {
				var renderer = ZoneRenderer.get(zone.shape().type());

				if (renderer != null) {
					var baseColor = zone.color().withAlpha(Mth.lerpInt((float) (dist / 10D), 100, 0));
					renderer.render(Cast.to(zone.shape()), new ZoneRenderer.Context(frame, baseColor, Color.TRANSPARENT));
				}
			}
		}
	}

	void render(T shape, Context ctx);
}
