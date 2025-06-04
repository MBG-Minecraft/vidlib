package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.kmath.Rotation;
import dev.latvian.mods.kmath.VoxelShapeBox;
import dev.latvian.mods.kmath.color.Color;
import net.minecraft.world.phys.Vec3;

public record CubeVisual(Vec3 pos, VoxelShapeBox shape, Color color, Color lineColor, Rotation rotation) implements Visual {
}
