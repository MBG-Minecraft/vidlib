package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.math.VoxelShapeBox;
import net.minecraft.world.phys.Vec3;

public record CubeVisual(Vec3 pos, VoxelShapeBox shape, Color color, Color lineColor, Rotation rotation) implements Visual {
}
