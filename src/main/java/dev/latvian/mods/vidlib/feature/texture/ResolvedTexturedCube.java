package dev.latvian.mods.vidlib.feature.texture;

import net.minecraft.world.phys.AABB;

public record ResolvedTexturedCube(AABB box, ResolvedCubeTextures textures) {
}
