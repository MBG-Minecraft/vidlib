package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.buffers.Std140Builder;
import net.minecraft.client.renderer.DynamicGpuDataStorage;
import org.joml.Vector2fc;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;

public record ChromaKeyData(
	Vector4fc keyColor,
	Vector2fc keyCC,
	Vector2fc range
) implements DynamicGpuDataStorage.DynamicGpuData {
	@Override
	public void write(ByteBuffer buffer) {
		Std140Builder.intoBuffer(buffer)
			.putVec4(keyColor)
			.putVec2(keyCC)
			.putVec2(range)
		;
	}
}
