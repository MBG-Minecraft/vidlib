package dev.latvian.mods.vidlib.util;

import net.minecraft.world.phys.AABB;

public record CachedCube(AABB box, ResolvedCubeTextures textures) {
}
