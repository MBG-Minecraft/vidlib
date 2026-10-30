package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.klib.platform.PlatformType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryValueInfo;
import dev.latvian.mods.klib.registry.RefOfKey;
import net.minecraft.core.RegistryAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CustomRegistry.class)
public abstract class CustomRegistryMixin {

	@Shadow
	public abstract boolean syncValues();

	@Shadow
	public abstract String registryId();

	/**
	 * KLib's CustomRegistry.updateValuesAndRefs() nulls the value of every RefOfKey stored in refMap and
	 * then only re-binds the refs that are *different* objects from the ones in valueList. Since
	 * updateValues()/readValues() re-use the very same RefOfKey instances, the re-bind condition
	 * (ref0 != ref) is never true and the live values are left null.
	 * <p>
	 * As soon as the registry is synchronized to a client, writeValues() calls Ref#value() on one of those
	 * null refs and throws NullPointerException("Value of " + key + " isn't bound"), which aborts the
	 * datapack sync and kicks the joining player with "Invalid player data" (singleplayer included).
	 * <p>
	 * Dropping the null-ing write keeps the bound values alive. Values of keys that are no longer present
	 * are simply refreshed by the next updateValues()/readValues() call.
	 */
	@Redirect(method = "updateValuesAndRefs", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Ldev/latvian/mods/klib/registry/RefOfKey;value:Ljava/lang/Object;", ordinal = 0))
	private void vl$keepBoundValue(RefOfKey<?> ref, Object value) {
		// no-op: keep the value that updateValues()/readValues() just bound
	}

	/**
	 * Registries created with createNoValueSync() (e.g. gradient's client_gradient) explicitly opt out of
	 * value syncing - their values only exist client-side, loaded from resource packs. KLib honours that in
	 * buildAllMeta() but not in syncAll()/writeValues(), so every player join tries to encode the
	 * client-only values, fails to find a type for them and spams "Failed to write custom registry".
	 * Report an empty payload for them instead.
	 */
	@Inject(method = "writeValues", at = @At("HEAD"), cancellable = true)
	private void vl$skipNoValueSync(RegistryAccess registryAccess, PlatformType platformType, CallbackInfoReturnable<CustomRegistryValueInfo> cir) {
		if (!syncValues()) {
			cir.setReturnValue(new CustomRegistryValueInfo(registryId(), List.of()));
		}
	}

	/**
	 * Counterpart to writeValues(): don't let a no-value-sync registry be replaced by its (empty) payload
	 * on the client.
	 */
	@Inject(method = "readValues", at = @At("HEAD"), cancellable = true)
	private void vl$skipNoValueSyncRead(CustomRegistryValueInfo info, RegistryAccess registryAccess, PlatformType platformType, CallbackInfo ci) {
		if (!syncValues()) {
			ci.cancel();
		}
	}
}
