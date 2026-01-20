package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(PoiSection.class)
public class PoiSectionMixin {
	@Inject(method = "add(Lnet/minecraft/world/entity/ai/village/poi/PoiRecord;)Z", at = @At("HEAD"), cancellable = true)
	private void vl$add(PoiRecord poiRecord, CallbackInfoReturnable<Boolean> cir) {
		if (CommonGameEngine.INSTANCE.disablePOI()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "add(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Holder;)V", at = @At("HEAD"), cancellable = true)
	private void vl$add(BlockPos pos, Holder<PoiType> type, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.disablePOI()) {
			ci.cancel();
		}
	}

	@Inject(method = "remove", at = @At("HEAD"), cancellable = true)
	private void vl$remove(BlockPos pos, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.disablePOI()) {
			ci.cancel();
		}
	}

	@Inject(method = "exists", at = @At("HEAD"), cancellable = true)
	private void vl$exists(BlockPos pos, Predicate<Holder<PoiType>> typePredicate, CallbackInfoReturnable<Boolean> cir) {
		if (CommonGameEngine.INSTANCE.disablePOI()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "getRecords", at = @At("HEAD"), cancellable = true)
	private void vl$getRecords(Predicate<Holder<PoiType>> typePredicate, PoiManager.Occupancy status, CallbackInfoReturnable<Stream<PoiRecord>> cir) {
		if (CommonGameEngine.INSTANCE.disablePOI()) {
			cir.setReturnValue(Stream.empty());
		}
	}
}
