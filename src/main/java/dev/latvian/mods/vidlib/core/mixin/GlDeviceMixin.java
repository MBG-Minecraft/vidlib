package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import dev.latvian.mods.vidlib.core.VLDirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlDevice")
public abstract class GlDeviceMixin implements VLDirectStateAccess {
	@Shadow
	public abstract DirectStateAccess directStateAccess();

	@Override
	public int vl$createFrameBufferObject() {
		return ((VLDirectStateAccess) directStateAccess()).vl$createFrameBufferObject();
	}

	@Override
	public DirectStateAccess vl$directStateAccess() {
		return directStateAccess();
	}
}
