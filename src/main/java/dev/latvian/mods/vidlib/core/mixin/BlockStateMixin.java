package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.klib.util.WithCache;
import dev.latvian.mods.vidlib.core.VLBlockState;
import dev.latvian.mods.vidlib.feature.block.VidLibBlockStateClientProperties;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public class BlockStateMixin implements VLBlockState {
	@Unique
	private Object vl$clientProperties;

	@Override
	public void vl$clearCache() {
		if (vl$clientProperties instanceof WithCache cache) {
			cache.clearCache();
			vl$clientProperties = null;
		}
	}

	@Override
	public Object vl$clientProperties() {
		if (vl$clientProperties == null) {
			vl$clientProperties = new VidLibBlockStateClientProperties((BlockState) (Object) this);
		}

		return vl$clientProperties;
	}
}
