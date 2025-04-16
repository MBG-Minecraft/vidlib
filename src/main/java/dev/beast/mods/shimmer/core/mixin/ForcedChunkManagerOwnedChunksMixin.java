package dev.beast.mods.shimmer.core.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.minecraft.core.UUIDUtil;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.common.world.chunk.TicketSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(ForcedChunkManager.OwnedChunks.class)
public class ForcedChunkManagerOwnedChunksMixin {
	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;unboundedMap(Lcom/mojang/serialization/Codec;Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/codecs/UnboundedMapCodec;", ordinal = 1))
	private static UnboundedMapCodec<UUID, TicketSet> shimmer$fixEntityCodec(Codec<UUID> key, Codec<TicketSet> value) {
		return Codec.unboundedMap(UUIDUtil.STRING_CODEC, value);
	}
}
