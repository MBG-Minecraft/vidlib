package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.VoxelShapeBox;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
import dev.latvian.mods.klib.render.SphereRenderer;
import dev.latvian.mods.klib.shape.SpherePoints;
import dev.latvian.mods.klib.texture.LightUV;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.core.VLBlockInWorld;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.visual.TexturedCubeRenderer;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import dev.latvian.mods.vidlib.feature.zone.shape.CylinderZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.RotatedBoxZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.SphereZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.UniverseZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShapeGroup;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public interface ZoneRenderer<T extends ZoneShape> {
	Map<SimpleRegistryType<?>, ZoneRenderer<?>> RENDERERS = new IdentityHashMap<>();

	record Context(FrameInfo frame, Color color, Color outlineColor, boolean outerBounds) {
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
		ZoneRenderer.register(CylinderZoneShape.TYPE, new CylinderZoneRenderer());
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
		var renderType = VidLibClientOptions.getZoneRenderType();
		var buffers = frame.buffers();
		var session = frame.session();
		var clip = session.zoneClip;

		if (clip != null && clip.pos() != null) {
			ms.pushPose();
			frame.translate(clip.pos());
			SphereRenderer.lines(ms, 0F, 0F, 0F, 0.25F, SpherePoints.L, buffers, BufferSupplier.DEBUG_NO_DEPTH, Color.BLACK);
			ms.popPose();
		}

		if (renderType == ZoneRenderType.COLLISIONS) {
			for (var sz : session.filteredZones.getSolidZones()) {
				if (sz.instance().zone.shape().closestDistanceTo(cameraPos) <= 2048D && frame.isVisible(sz.instance().zone.shape().getBoundingBox())) {
					boolean hovered = clip != null && clip.instance() == sz.instance();
					var baseColor = sz.instance().zone.color().withAlpha(50);
					var outlineColor = hovered ? Color.WHITE : sz.instance().entities.isEmpty() ? sz.instance().zone.color() : Color.GREEN;
					CuboidRenderer.voxelShapeBox(ms, sz.shapeBox(), cameraPos.reverse(), buffers, BufferSupplier.DEBUG_NO_DEPTH, false, baseColor, outlineColor);
				}
			}
		} else {
			boolean outerBounds = VidLibClientOptions.getShowZoneOuterBounds();

			for (var container : session.filteredZones) {
				for (var instance : container.zones) {
					if (instance.zone.shape().closestDistanceTo(cameraPos) <= 2048D && frame.isVisible(instance.zone.shape().getBoundingBox())) {
						var renderer = ZoneRenderer.get(instance.zone.shape().type());

						if (renderer != EmptyZoneRenderer.INSTANCE) {
							boolean hovered = clip != null && clip.instance() == instance;
							var baseColor = instance.zone.color().withAlpha(50);
							var outlineColor = hovered ? Color.WHITE : instance.entities.isEmpty() ? instance.zone.color() : Color.GREEN;

							if (renderType == ZoneRenderType.NORMAL) {
								renderer.render(Cast.to(instance.zone.shape()), new ZoneRenderer.Context(frame, baseColor, outlineColor, outerBounds));
							} else if (renderType == ZoneRenderType.BLOCKS) {
								if (session.cachedZoneShapes == null) {
									session.cachedZoneShapes = new IdentityHashMap<>();
								}

								var voxelShape = session.cachedZoneShapes.get(instance.zone.shape());

								if (voxelShape == null) {
									voxelShape = VoxelShapeBox.EMPTY;
									session.cachedZoneShapes.put(instance.zone.shape(), voxelShape);

									Thread.startVirtualThread(() -> {
										var filter = VidLibClientOptions.getZoneBlockFilter();

										session.cachedZoneShapes.put(instance.zone.shape(), VoxelShapeBox.of(instance.zone.shape().createBlockRenderingShape(pos -> {
											var state = mc.level.getBlockState(pos);

											if (state.isAir()) {
												return false;
											}

											return filter == BlockFilter.ANY.instance() || filter.test(VLBlockInWorld.of(mc.level, pos, state));
										}).optimize()));
									});
								}

								CuboidRenderer.voxelShapeBox(ms, voxelShape, cameraPos.reverse(), buffers, BufferSupplier.DEBUG_NO_DEPTH, true, baseColor, outlineColor);
							}
						}
					}
				}
			}
		}
	}

	static void renderVisible(FrameInfo frame) {
		for (var sz : frame.session().filteredZones.getVisible()) {
			var zone = sz.instance().zone;
			double dist = zone.shape().closestDistanceTo(frame.camera().getPosition());

			if (dist > 2048D || !frame.isVisible(zone.shape().getBoundingBox())) {
				continue;
			}

			for (var cube : sz.cachedCubes().getOrDefault(frame.layer(), List.of())) {
				TexturedCubeRenderer.render(frame, LightUV.FULLBRIGHT, cube, Color.WHITE);
			}
		}
	}

	static void renderSolid(FrameInfo frame) {
		for (var sz : frame.session().filteredZones.getSolidZones()) {
			var zone = sz.instance().zone;
			double dist = zone.shape().closestDistanceTo(frame.camera().getPosition());

			if (dist <= 10D && zone.color().alpha() > 0 && frame.isVisible(zone.shape().getBoundingBox()) && zone.solid().test(frame.mc().player)) {
				var renderer = ZoneRenderer.get(zone.shape().type());

				if (renderer != null) {
					var baseColor = zone.color().withAlpha(Mth.lerpInt((float) (dist / 10D), 100, 0));
					renderer.render(Cast.to(zone.shape()), new ZoneRenderer.Context(frame, baseColor, Color.TRANSPARENT, false));
				}
			}
		}
	}

	void render(T shape, Context ctx);

	/*
	void buildLines(T shape, Context ctx, VertexCallback callback);

	default void buildOuterLines(T shape, Context ctx, VertexCallback callback) {
	}

	void buildQuads(T shape, Context ctx, VertexCallback callback);
	 */
}
