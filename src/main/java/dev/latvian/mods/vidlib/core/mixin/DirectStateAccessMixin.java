package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import dev.latvian.mods.vidlib.core.VLDirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DirectStateAccess.class)
public abstract class DirectStateAccessMixin implements VLDirectStateAccess {
	@Override
	@Invoker("createFrameBufferObject")
	public abstract int vl$createFrameBufferObject();
}
