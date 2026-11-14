package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.klib.util.ID;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

public class DepthOfFieldCanvas {
	public static final Matrix4f inverseWorldMatrix = new Matrix4f();
	public static final Vector3f focusPos = new Vector3f();
	public static float focusRange = 0F;
	public static float blurRange = 0F;
	public static float strength = 0F;
	public static int shape = 0;
	public static int blurMode = 0;
	public static final Vector4f debugNearColor = ARGB.vector4fFromARGB32(0x7F00FF00);
	public static final Vector4f debugFarColor = ARGB.vector4fFromARGB32(0x7FFF0000);

	public static final Canvas CANVAS = new Canvas(
		ID.vidlib("depth_of_field"),
		() -> strength > 0F,
		"VidLibDepthOfField",
		List.of(
			UniformSupplier.of("InverseViewProjectionMat", inverseWorldMatrix),
			UniformSupplier.of("FocusPos", focusPos),
			UniformSupplier.of("FocusRange", () -> focusRange),
			UniformSupplier.of("BlurRange", () -> blurRange),
			UniformSupplier.of("Strength", () -> strength),
			UniformSupplier.of("Shape", () -> shape),
			UniformSupplier.of("BlurMode", () -> blurMode),
			UniformSupplier.of("DebugNearCol", debugNearColor),
			UniformSupplier.of("DebugFarCol", debugFarColor)
		)
	);
}
