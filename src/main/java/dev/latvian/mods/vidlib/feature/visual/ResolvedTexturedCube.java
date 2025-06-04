package dev.latvian.mods.vidlib.feature.visual;

import net.minecraft.world.phys.AABB;

public record ResolvedTexturedCube(AABB box, ResolvedCubeTextures textures) implements Visual {
}
