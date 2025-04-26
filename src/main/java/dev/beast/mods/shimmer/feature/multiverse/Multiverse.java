package dev.beast.mods.shimmer.feature.multiverse;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class Multiverse {
	public static final ResourceKey<Level> LOBBY = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("lobby"));
}
