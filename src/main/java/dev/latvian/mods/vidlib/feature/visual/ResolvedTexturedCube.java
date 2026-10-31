package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.klib.texture.ResolvedCubeTextures;
import net.minecraft.world.phys.AABB;

public record ResolvedTexturedCube(AABB box, ResolvedCubeTextures textures) implements Visual {
}
