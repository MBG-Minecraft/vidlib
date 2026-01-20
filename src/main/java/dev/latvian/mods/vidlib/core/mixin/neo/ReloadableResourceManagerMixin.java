package dev.latvian.mods.vidlib.core.mixin.neo;

import dev.latvian.mods.vidlib.feature.dynamicresources.DynamicResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(value = ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {
	@Shadow
	@Final
	private PackType type;

	@ModifyArg(method = "createReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/MultiPackResourceManager;<init>(Lnet/minecraft/server/packs/PackType;Ljava/util/List;)V"), index = 1)
	private List<PackResources> vl$loaders(List<PackResources> original) {
		return DynamicResources.injectPackResources(type, original);
	}
}
