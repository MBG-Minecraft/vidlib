package dev.latvian.mods.vidlib.feature.skybox;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.world.level.material.FogType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClientFogOverride {
	public static final Map<Integer, FogShape> SHAPES = Arrays.stream(FogShape.values()).collect(Collectors.toMap(FogShape::getIndex, Function.identity()));

	public static FogParameters override = FogParameters.NO_FOG;

	public static FogParameters get(FogParameters shaderFog) {
		if (Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera() != FogType.NONE) {
			return shaderFog;
		}

		return override != null ? override : shaderFog;
	}

	@Nullable
	public static FogParameters convert(FogOverride f) {
		if (f.color().argb() == 0) {
			if (f.shape() == 0) {
				return FogParameters.NO_FOG;
			} else {
				return null;
			}
		} else {
			var c = f.color();
			return new FogParameters(
				f.range().min(),
				f.range().max(),
				SHAPES.getOrDefault(f.shape(), FogShape.SPHERE),
				c.redf(),
				c.greenf(),
				c.bluef(),
				c.alphaf()
			);
		}
	}
}
