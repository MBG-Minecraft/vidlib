package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.core.ShimmerPayloadRegistrar;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.camerashake.CameraShakeType;
import dev.beast.mods.shimmer.feature.camerashake.ShakeCameraPayload;
import dev.beast.mods.shimmer.feature.camerashake.StopCameraShakingPayload;
import dev.beast.mods.shimmer.feature.clock.SyncClockFontsPayload;
import dev.beast.mods.shimmer.feature.clock.SyncClockInstancePayload;
import dev.beast.mods.shimmer.feature.clock.SyncClocksPayload;
import dev.beast.mods.shimmer.feature.cutscene.PlayCutscenePayload;
import dev.beast.mods.shimmer.feature.cutscene.StopCutscenePayload;
import dev.beast.mods.shimmer.feature.cutscene.event.CutsceneEvent;
import dev.beast.mods.shimmer.feature.data.SyncPlayerDataPayload;
import dev.beast.mods.shimmer.feature.data.SyncServerDataPayload;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.misc.CreateFireworksPayload;
import dev.beast.mods.shimmer.feature.misc.FakeBlockPayload;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.feature.misc.RefreshNamePayload;
import dev.beast.mods.shimmer.feature.misc.SetPostEffectPayload;
import dev.beast.mods.shimmer.feature.misc.SyncPlayerTagsPayload;
import dev.beast.mods.shimmer.feature.session.RemovePlayerDataPayload;
import dev.beast.mods.shimmer.feature.toolitem.PositionToolItem;
import dev.beast.mods.shimmer.feature.toolitem.ToolItem;
import dev.beast.mods.shimmer.feature.zone.SyncZonesPayload;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShape;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumber;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import dev.beast.mods.shimmer.util.KnownCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
	@SubscribeEvent
	public static void afterLoad(FMLLoadCompleteEvent event) {
		KnownCodec.bootstrap();
		WorldNumber.bootstrap();
		WorldPosition.bootstrap();
		EntityFilter.bootstrap();
		BlockFilter.bootstrap();
		ZoneShape.bootstrap();
		CameraShakeType.bootstrap();
		InternalPlayerData.bootstrap();
		CutsceneEvent.bootstrap();

		ToolItem.REGISTRY.put("pos", new PositionToolItem());
	}

	@SubscribeEvent
	public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		var reg = ShimmerPayloadRegistrar.of(event);

		reg.s2c(SyncPlayerDataPayload.TYPE);
		reg.s2c(SyncServerDataPayload.TYPE);
		reg.s2c(SyncZonesPayload.TYPE);
		reg.s2c(SyncClockFontsPayload.TYPE);
		reg.s2c(SyncClocksPayload.TYPE);

		reg.s2c(RemovePlayerDataPayload.TYPE);
		reg.s2c(FakeBlockPayload.TYPE);
		reg.s2c(PlayCutscenePayload.TYPE);
		reg.s2c(StopCutscenePayload.TYPE);
		reg.s2c(ShakeCameraPayload.TYPE);
		reg.s2c(StopCameraShakingPayload.TYPE);
		reg.s2c(SetPostEffectPayload.TYPE);
		reg.s2c(SyncClockInstancePayload.TYPE);
		reg.s2c(CreateFireworksPayload.TYPE);
		reg.s2c(RefreshNamePayload.TYPE);
		reg.s2c(SyncPlayerTagsPayload.TYPE);
	}

	@SubscribeEvent
	public static void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
			for (var entry : ToolItem.REGISTRY.entrySet()) {
				var stack = entry.getValue().createItem();
				stack.set(DataComponents.ITEM_NAME, entry.getValue().getName());
				var tag = new CompoundTag();
				tag.putString("shimmer:tool", entry.getKey());
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
				stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
				event.accept(stack);
			}
		}
	}
}
