package dev.latvian.mods.vidlib.core.mixin.client;

import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import dev.latvian.mods.vidlib.core.api.client.VLDirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DirectStateAccess.class)
public abstract class DirectStateAccessMixin implements VLDirectStateAccess {
	@Override
	@Invoker("createFrameBufferObject")
	public abstract int vl$createFrameBufferObject();
}
