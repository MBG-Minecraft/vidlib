package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.vidlib.core.api.VLObject;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public class BlockStateMixin implements VLObject {
	@Unique
	private Object vl$clientProperties;

	@Override
	public Object vl$get(int id) {
		return id == 0 ? vl$clientProperties : null;
	}

	@Override
	public void vl$set(int id, Object value) {
		if (id == 0) {
			vl$clientProperties = value;
		}
	}
}
