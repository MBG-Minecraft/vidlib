package dev.latvian.mods.vidlib.feature.location;

import dev.latvian.mods.vidlib.core.VLMinecraftEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public record WarpLocation(String id, String displayName, ResourceKey<Level> dimension, Function<VLMinecraftEnvironment, BlockPos> pos, boolean admin) {
	public WarpLocation(String id, String displayName, Function<VLMinecraftEnvironment, BlockPos> pos) {
		this(id, displayName, Level.OVERWORLD, pos, true);
	}
}
