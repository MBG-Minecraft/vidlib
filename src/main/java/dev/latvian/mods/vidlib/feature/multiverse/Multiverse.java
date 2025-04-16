package dev.latvian.mods.vidlib.feature.multiverse;

import dev.latvian.mods.vidlib.feature.registry.ID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class Multiverse {
	public static final ResourceKey<Level> LOBBY = ResourceKey.create(Registries.DIMENSION, ID.mc("lobby"));
}
