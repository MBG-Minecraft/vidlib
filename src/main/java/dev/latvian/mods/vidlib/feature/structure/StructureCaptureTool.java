package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.block.ConnectedBlock;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public enum StructureCaptureTool implements VidLibTool, PlayerActionHandler {
	@AutoRegister
	INSTANCE;

	@Override
	public String getId() {
		return "structure_capture";
	}

	@Override
	public Component getName() {
		return Component.literal("Structure Capture Tool");
	}

	@Override
	public ResourceLocation getModel() {
		return ResourceLocation.withDefaultNamespace("echo_shard");
	}

	@Override
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (hit != null && player.level() instanceof ServerLevel level) {
			var origin = hit.getBlockPos();

			Thread.startVirtualThread(() -> {
				player.tell(Component.literal("Scanning the structure..."));
				var blocks = new Long2ObjectOpenHashMap<BlockState>();

				boolean captureAll = false;

				var tag = item.get(DataComponents.CUSTOM_DATA);

				if (tag != null) {
					captureAll = tag.getUnsafe().getBooleanOr("structure_capture_full", false);
				}

				level.walkBlocks(ConnectedBlock.WalkType.DIAGONAL, origin, StructureCapture.buildFilter(), !captureAll, 2048, c -> {
					blocks.put(c.block().pos().asLong(), c.block().state());

					if (StructureCapture.PARTICLES.get()) {
						level.cubeParticles(StructureCapture.PARTICLE, List.of(c.block().pos()));
					}

					return false;
				});

				level.getServer().executeBlocking(() -> {
					player.tell(Component.literal("Added %,d blocks".formatted(blocks.size())));
					StructureCapture.CURRENT.getValue().blocks.putAll(blocks);
				});
			});

		}

		return true;
	}

	@Override
	public Set<PlayerActionType> getHandledPlayerActions() {
		return PlayerActionType.SWAP_SET;
	}

	@Override
	public void onPlayerAction(ServerPlayer player, ItemStack item, InteractionHand hand, PlayerActionType action) {
		if (action == PlayerActionType.SWAP) {
			var tag = item.get(DataComponents.CUSTOM_DATA);

			if (tag != null) {
				var data = tag.copyTag();
				data.putBoolean("structure_capture_full", !data.getBooleanOr("structure_capture_full", false));
				item.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
			}
		}
	}
}
