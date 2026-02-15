package dev.latvian.mods.vidlib.core.mixin.neo;

import dev.latvian.mods.vidlib.core.VLTicketOwner;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.neoforged.neoforge.common.world.chunk.ForcedChunkManager$TicketOwner")
public abstract class TicketOwnerMixin<T extends Comparable<? super T>> implements VLTicketOwner<T> {
	@Override
	@Accessor("id")
	public abstract ResourceLocation vl$getId();

	@Override
	@Accessor("owner")
	public abstract T vl$getOwner();
}
