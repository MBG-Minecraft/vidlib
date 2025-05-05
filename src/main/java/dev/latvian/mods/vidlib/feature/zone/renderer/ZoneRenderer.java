package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.kmath.SpherePoints;
import dev.latvian.mods.kmath.VoxelShapeBox;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.render.BoxRenderer;
import dev.latvian.mods.kmath.render.SphereRenderer;
import dev.latvian.mods.kmath.texture.LightUV;
import dev.latvian.mods.vidlib.core.VLBlockInWorld;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.client.DynamicSpriteTexture;
import dev.latvian.mods.vidlib.feature.client.FluidBoxRenderer;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import dev.latvian.mods.vidlib.feature.zone.shape.RotatedBoxZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.SphereZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.UniverseZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShapeGroup;
import dev.latvian.mods.vidlib.util.Cast;
import dev.latvian.mods.vidlib.util.FrameInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
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

											return filter == BlockFilter.ANY.instance() || filter.test(VLBlockInWorld.of(mc.level, pos, state));
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

	static void renderFluid(FrameInfo frame) {
		for (var sz : frame.session().filteredZones.getFluidZones()) {
			var zone = sz.instance().zone;
			double dist = zone.shape().closestDistanceTo(frame.camera().getPosition());

			if (dist <= 2048D && frame.frustum().isVisible(zone.shape().getBoundingBox())) {
				var fluidState = sz.instance().zone.fluid();

				var yOff = 1F - fluidState.getOwnHeight();
				yOff += (float) Math.clamp(-frame.y(yOff) / 50D, 0D, 0.5D);

				var stillTexture = DynamicSpriteTexture.getStillFluid(frame.mc(), fluidState.getFluidType());
				var flowingTexture = DynamicSpriteTexture.getFlowingFluid(frame.mc(), fluidState.getFluidType());

				for (var box : sz.shapeBox().boxes()) {
					var bminX = box.minX;
					var bminY = box.minY;
					var bminZ = box.minZ;
					var bmaxX = box.maxX;
					var bmaxY = box.maxY - yOff;
					var bmaxZ = box.maxZ;

					var blockPos = BlockPos.containing(Mth.floor(Mth.lerp(0.5D, bminX, bmaxX)), bmaxY - 1D, Mth.floor(Mth.lerp(0.5D, bminZ, bmaxZ)));
					var color = Color.of(0xFF000000 | frame.mc().getBlockColors().getColor(fluidState.createLegacyBlock(), frame.mc().level, blockPos, 0));

					FluidBoxRenderer.render(
						frame,
						fluidState,
						color,
						LightUV.FULLBRIGHT,
						bminX,
						bminY,
						bminZ,
						bmaxX,
						bmaxY,
						bmaxZ,
						stillTexture,
						flowingTexture
					);
				}
			}
		}
	}

	void render(T shape, Context ctx);
}
