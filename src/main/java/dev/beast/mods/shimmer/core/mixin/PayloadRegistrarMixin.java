package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerPayloadRegistrar;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PayloadRegistrar.class)
public class PayloadRegistrarMixin implements ShimmerPayloadRegistrar {
}
