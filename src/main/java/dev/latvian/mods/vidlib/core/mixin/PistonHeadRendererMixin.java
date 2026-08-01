package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PistonHeadRenderer.class)
public abstract class PistonHeadRendererMixin implements BlockEntityRenderer<PistonMovingBlockEntity> {
	/**
	 * @author Lat
	 * @reason MBG
	 */
	@Override
	@Overwrite
	public int getViewDistance() {
		return 512;
	}

	@Override
	public boolean shouldRender(PistonMovingBlockEntity blockEntity, Vec3 cameraPos) {
		return true;
	}
}
