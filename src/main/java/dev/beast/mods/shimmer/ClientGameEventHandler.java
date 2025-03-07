package dev.beast.mods.shimmer;

import com.mojang.math.Axis;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.clock.ClockRenderer;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import dev.beast.mods.shimmer.feature.misc.DebugTextEvent;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.feature.structure.GhostStructure;
import dev.beast.mods.shimmer.feature.toolitem.ToolItem;
import dev.beast.mods.shimmer.feature.zone.renderer.EmptyZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import dev.beast.mods.shimmer.math.BoxRenderer;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.math.VoxelShapeBox;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.IdentityHashMap;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGameEventHandler {
	@SubscribeEvent
	public static void clientPreTick(ClientTickEvent.Pre event) {
		Minecraft.getInstance().shimmer$preTick();
	}

	@SubscribeEvent
	public static void clientPostTick(ClientTickEvent.Post event) {
		Minecraft.getInstance().shimmer$postTick();
	}

	@SubscribeEvent
	public static void renderWorld(RenderLevelStageEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var session = mc.player.shimmer$sessionData();
		float delta = event.getPartialTick().getGameTimeDeltaPartialTick(true);

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
			Minecraft.getInstance().shimmer$renderSetup(event, delta);
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			var cameraPos = event.getCamera().getPosition();
			var ms = event.getPoseStack();
			var frustum = event.getFrustum();
			var localData = mc.player.get(InternalPlayerData.LOCAL);

			if (localData.renderZones) {
				if (localData.zoneRenderType == 1) {
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

									if (localData.zoneRenderType == 0) {
										renderer.render(Cast.to(instance.zone.shape()), new ZoneRenderer.Context(mc, ms, cameraPos, frustum, delta, baseColor, outlineColor));
									} else if (localData.zoneRenderType == 2) {
										if (localData.cachedZoneShapes == null) {
											localData.cachedZoneShapes = new IdentityHashMap<>();
										}

										var voxelShape = localData.cachedZoneShapes.get(instance.zone.shape());

										if (voxelShape == null) {
											voxelShape = VoxelShapeBox.EMPTY;
											localData.cachedZoneShapes.put(instance.zone.shape(), voxelShape);

											Thread.startVirtualThread(() -> {
												localData.cachedZoneShapes.put(instance.zone.shape(), VoxelShapeBox.of(instance.zone.shape().createBlockRenderingShape(pos -> {
													var block = new BlockInWorld(mc.level, pos, true);
													return !block.getState().isAir() && (localData.zoneBlockFilter == BlockFilter.NONE.instance() || localData.zoneBlockFilter.test(block));
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

			for (var instance : session.clocks.values()) {
				if (instance.clock.dimension() == mc.level.dimension()) {
					for (var location : instance.clock.locations()) {
						ClockRenderer.render(mc, instance, location, ms, cameraPos, delta);
					}
				}
			}

			if (!GhostStructure.LIST.isEmpty()) {
				for (var gs : GhostStructure.LIST) {
					if (gs.visibleTo().test(mc.player)) {
						ms.pushPose();
						ms.translate(gs.pos().x - cameraPos.x, gs.pos().y - cameraPos.y, gs.pos().z - cameraPos.z);
						ms.scale((float) gs.scale().x, (float) gs.scale().y, (float) gs.scale().z);
						ms.mulPose(Axis.YP.rotationDegrees((float) gs.rotation().y));
						ms.mulPose(Axis.XP.rotationDegrees((float) gs.rotation().x));
						ms.mulPose(Axis.ZP.rotationDegrees((float) gs.rotation().z));
						gs.structure().render(ms);
						ms.popPose();
					}
				}
			}

			var cc = ClientCutscene.instance;

			if (cc != null) {
				for (var task : cc.steps) {
					if (task.render != null && cc.totalTick >= task.start && cc.totalTick <= task.start + task.length) {
						float tick = Math.max(cc.totalTick - 1 + delta, 0F);
						var progress = KMath.clamp((tick - task.start) / (float) task.length, 0F, 1F);

						if (progress < 1F) {
							var target = task.prevRenderTarget == null || task.renderTarget == null ? cc.prevTarget.lerp(cc.target, delta) : task.prevRenderTarget.lerp(task.renderTarget, delta);

							for (var render : task.render) {
								render.render(mc, event, delta, progress, target);
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void renderHUD(RenderGuiEvent.Post event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null || mc.options.hideGui) {
			return;
		}

		if (mc.isLocalServer() || mc.player.hasPermissions(2)) {
			var tool = ToolItem.of(mc.player);

			if (tool != null) {
				tool.getSecond().drawText(tool.getFirst(), mc.player, mc.hitResult, DebugTextEvent.LEFT, DebugTextEvent.RIGHT);
			}

			NeoForge.EVENT_BUS.post(new DebugTextEvent());

			var zoneClip = mc.player.shimmer$sessionData().zoneClip;

			if (zoneClip != null) {
				var component = Component.literal("Zone: ").append(Component.literal(zoneClip.instance().container.id.toString()).withStyle(ChatFormatting.AQUA));

				if (zoneClip.instance().container.zones.size() > 1) {
					component.append(Component.literal("[" + zoneClip.instance().index + "]").withStyle(ChatFormatting.GREEN));
				}

				DebugTextEvent.LEFT.add(component);
			}

			for (int i = 0; i < DebugTextEvent.LEFT.size(); i++) {
				int w = mc.font.width(DebugTextEvent.LEFT.get(i));
				int x = 2;
				int y = 2 + i * 12;
				event.getGuiGraphics().fill(x, y, x + w + 6, y + 12, 0xA0000000);
				event.getGuiGraphics().drawString(mc.font, DebugTextEvent.LEFT.get(i), x + 3, y + 2, 0xFFFFFFFF, true);
			}

			for (int i = 0; i < DebugTextEvent.RIGHT.size(); i++) {
				int w = mc.font.width(DebugTextEvent.RIGHT.get(i));
				int x = event.getGuiGraphics().guiWidth() - w - 8;
				int y = 2 + i * 12;
				event.getGuiGraphics().fill(x, y, x + w + 6, y + 12, 0xA0000000);
				event.getGuiGraphics().drawString(mc.font, DebugTextEvent.RIGHT.get(i), x + 3, y + 2, 0xFFFFFFFF, true);
			}

			DebugTextEvent.LEFT.clear();
			DebugTextEvent.RIGHT.clear();
		}
	}

	@SubscribeEvent
	public static void adjustFOV(ViewportEvent.ComputeFov event) {
		var mc = Minecraft.getInstance();
		var override = CameraOverride.get(mc);

		if (override != null) {
			event.setFOV(event.getFOV() * override.getZoom(event.getPartialTick()));
		}

		/*
		if (mc.player != null && mc.player.getVehicle() != null) {
			event.setFOV(event.getFOV() * mc.player.getVehicle().adjustFOV(camera, event.getPartialTick(), changingFov));
		}
		 */
	}

	@SubscribeEvent
	public static void renderBlockHighlight(RenderHighlightEvent.Block event) {
		var mc = Minecraft.getInstance();

		if (mc.player != null && mc.level != null && !mc.player.isSpectatorOrCreative() && mc.level.getBlockState(event.getTarget().getBlockPos()).is(Blocks.BARRIER)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void debugText(CustomizeGuiOverlayEvent.DebugText event) {
		var mc = Minecraft.getInstance();

		// event.getLeft().clear();
		// event.getRight().clear();
		// event.getLeft().add(mc.fpsString);
	}
}
