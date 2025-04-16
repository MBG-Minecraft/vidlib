package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLPayloadRegistrar;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PayloadRegistrar.class)
public class PayloadRegistrarMixin implements VLPayloadRegistrar {
}
