package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.ShimmerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin extends HumanoidModel<PlayerRenderState> {
	@Shadow
	@Final
	public ModelPart leftSleeve;

	@Shadow
	@Final
	public ModelPart rightSleeve;

	@Shadow
	@Final
	public ModelPart leftPants;

	@Shadow
	@Final
	public ModelPart rightPants;

	@Shadow
	@Final
	public ModelPart jacket;

	public PlayerModelMixin(ModelPart root) {
		super(root);
	}

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At("RETURN"))
	private void shimmer$setupAnim(PlayerRenderState state, CallbackInfo ci) {
		if (ShimmerConfig.limitClothingRendering) {
			var visible = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(state.x, state.y, state.z) <= ShimmerConfig.clothingRenderDistance * ShimmerConfig.clothingRenderDistance;

			leftSleeve.visible = visible;
			rightSleeve.visible = visible;
			leftPants.visible = visible;
			rightPants.visible = visible;
			jacket.visible = visible;
		}
	}
}
