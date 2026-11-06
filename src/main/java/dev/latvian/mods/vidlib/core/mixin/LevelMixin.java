package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.core.VLLevel;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModificationHolder;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Level.class)
public abstract class LevelMixin implements VLLevel {
	@Unique
	private final List<UndoableModificationHolder> vl$undoable = new ArrayList<>();

	@Unique
	private KNumberContext vl$knumberContext = null;

	@Inject(method = "tickBlockEntities", at = @At("RETURN"))
	private void vl$tickProps(CallbackInfo ci) {
		var props = VLPlatformHelper.INSTANCE.getProps(vl$level());
		props.tick(getEnvironment().getPauseType().tick());
	}

	@Override
	public List<UndoableModificationHolder> vl$getUndoableModifications() {
		return vl$undoable;
	}

	@Override
	public KNumberContext getGlobalContext() {
		if (vl$knumberContext == null) {
			vl$knumberContext = VLLevel.super.getGlobalContext();
		}

		return vl$knumberContext;
	}
}
