package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.block.ConnectedBlock;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum StructureCaptureToolItem implements VidLibTool {
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

				level.walkBlocks(ConnectedBlock.WalkType.DIAGONAL, origin, StructureCapture.buildFilter(), true, 2048, c -> {
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
}
