package dev.beast.mods.shimmer.feature.zone.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.beast.mods.shimmer.core.ShimmerBlockInWorld;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.misc.InternalData;
import dev.beast.mods.shimmer.feature.session.ShimmerLocalClientSessionData;
import dev.beast.mods.shimmer.feature.zone.ZoneRenderType;
import dev.beast.mods.shimmer.feature.zone.shape.RotatedBoxZoneShape;
import dev.beast.mods.shimmer.feature.zone.shape.SphereZoneShape;
import dev.beast.mods.shimmer.feature.zone.shape.UniverseZoneShape;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShape;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShapeGroup;
import dev.beast.mods.shimmer.math.BoxRenderer;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.SpherePoints;
import dev.beast.mods.shimmer.math.SphereRenderer;
import dev.beast.mods.shimmer.math.VoxelShapeBox;
import dev.beast.mods.shimmer.util.Cast;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.Map;

public interface ZoneRenderer<T extends ZoneShape> {
	Map<SimpleRegistryType<?>, ZoneRenderer<?>> RENDERERS = new IdentityHashMap<>();

	record Context(Minecraft mc, PoseStack poseStack, Vec3 cameraPos, Frustum frustum, float delta, Color color, Color outlineColor) {
		public MultiBufferSource buffers() {
			return mc.renderBuffers().bufferSource();
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

	static void renderAll(Minecraft mc, ShimmerLocalClientSessionData session, float delta, PoseStack ms, Vec3 cameraPos, Frustum frustum) {
		var renderType = mc.player.get(InternalData.ZONE_RENDER_TYPE);

		if (session.zoneClip != null && session.zoneClip.pos() != null) {
			ms.pushPose();
			ms.translate(session.zoneClip.pos().x - cameraPos.x, session.zoneClip.pos().y - cameraPos.y, session.zoneClip.pos().z - cameraPos.z);
			ms.scale(0.25F, 0.25F, 0.25F);
			SphereRenderer.renderDebugLines(SpherePoints.L, ms, mc.renderBuffers().bufferSource(), Color.BLACK);
			ms.popPose();
		}

		if (renderType == ZoneRenderType.COLLISIONS) {
			for (var sz : session.filteredZones.getSolidZones()) {
				if (cameraPos.closerThan(sz.instance().zone.shape().getCenterPos(), 512D) && frustum.isVisible(sz.instance().zone.shape().getBoundingBox())) {
					boolean hovered = session.zoneClip != null && session.zoneClip.instance() == sz.instance();
					var baseColor = sz.instance().zone.color().withAlpha(50);
					var outlineColor = hovered ? Color.WHITE : sz.instance().entities.isEmpty() ? baseColor : Color.GREEN;
					BoxRenderer.renderVoxelShape(ms, mc.renderBuffers().bufferSource(), sz.shapeBox(), cameraPos.reverse(), false, baseColor, outlineColor);
				}
			}
		} else {
			for (var container : session.filteredZones) {
				for (var instance : container.zones) {
					if (cameraPos.closerThan(instance.zone.shape().getCenterPos(), 512D) && frustum.isVisible(instance.zone.shape().getBoundingBox())) {
						var renderer = ZoneRenderer.get(instance.zone.shape().type());

						if (renderer != EmptyZoneRenderer.INSTANCE) {
							boolean hovered = session.zoneClip != null && session.zoneClip.instance() == instance;
							var baseColor = instance.zone.color().withAlpha(50);
							var outlineColor = hovered ? Color.WHITE : instance.entities.isEmpty() ? baseColor : Color.GREEN;

							if (renderType == ZoneRenderType.NORMAL) {
								renderer.render(Cast.to(instance.zone.shape()), new ZoneRenderer.Context(mc, ms, cameraPos, frustum, delta, baseColor, outlineColor));
							} else if (renderType == ZoneRenderType.BLOCKS) {
								if (session.cachedZoneShapes == null) {
									session.cachedZoneShapes = new IdentityHashMap<>();
								}

								var voxelShape = session.cachedZoneShapes.get(instance.zone.shape());

								if (voxelShape == null) {
									voxelShape = VoxelShapeBox.EMPTY;
									session.cachedZoneShapes.put(instance.zone.shape(), voxelShape);

									Thread.startVirtualThread(() -> {
										var filter = mc.player.get(InternalData.ZONE_BLOCK_FILTER);

										session.cachedZoneShapes.put(instance.zone.shape(), VoxelShapeBox.of(instance.zone.shape().createBlockRenderingShape(pos -> {
											var state = mc.level.getBlockState(pos);

											if (state.isAir()) {
												return false;
											}

											return filter == BlockFilter.ANY.instance() || filter.test(ShimmerBlockInWorld.of(mc.level, pos, state));
										}).optimize()));
									});
								}

								BoxRenderer.renderVoxelShape(ms, mc.renderBuffers().bufferSource(), voxelShape, cameraPos.reverse(), true, baseColor, outlineColor);
							}
						}
					}
				}
			}
		}
	}

	void render(T shape, Context ctx);
}
