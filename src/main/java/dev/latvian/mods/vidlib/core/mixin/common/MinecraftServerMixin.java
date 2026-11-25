package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.vidlib.core.api.VLObject;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements VLObject {
	@Unique
	private Object vl$sessionData;

	@Override
	public Object vl$get(int id) {
		return id == 522 ? vl$sessionData : null;
	}

	@Override
	public void vl$set(int id, Object value) {
		if (id == 522) {
			vl$sessionData = value;
		}
	}
}
