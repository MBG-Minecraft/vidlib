package dev.latvian.mods.vidlib.util;

import net.minecraft.world.phys.AABB;

public record ResolvedTexturedCube(AABB box, ResolvedCubeTextures textures) {
}
