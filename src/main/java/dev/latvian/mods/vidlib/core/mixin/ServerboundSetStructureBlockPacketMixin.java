package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.VidLibConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundSetStructureBlockPacket.class)
public class ServerboundSetStructureBlockPacketMixin {
	@Shadow
	@Final
	@Mutable
	private BlockPos offset;

	@Shadow
	@Final
	@Mutable
	private Vec3i size;

	@Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
	public void readSE(FriendlyByteBuf buf, CallbackInfo ci) {
		offset = new BlockPos(
			Mth.clamp(buf.readVarInt(), -VidLibConfig.structureBlockRange, VidLibConfig.structureBlockRange - 1),
			Mth.clamp(buf.readVarInt(), -VidLibConfig.structureBlockRange, VidLibConfig.structureBlockRange - 1),
			Mth.clamp(buf.readVarInt(), -VidLibConfig.structureBlockRange, VidLibConfig.structureBlockRange - 1)
		);

		size = new BlockPos(
			Mth.clamp(buf.readVarInt(), 0, VidLibConfig.structureBlockRange - 1),
			Mth.clamp(buf.readVarInt(), 0, VidLibConfig.structureBlockRange - 1),
			Mth.clamp(buf.readVarInt(), 0, VidLibConfig.structureBlockRange - 1)
		);
	}

	@Inject(method = "write", at = @At("RETURN"))
	public void writeSE(FriendlyByteBuf buf, CallbackInfo ci) {
		buf.writeVarInt(offset.getX());
		buf.writeVarInt(offset.getY());
		buf.writeVarInt(offset.getZ());
		buf.writeVarInt(size.getX());
		buf.writeVarInt(size.getY());
		buf.writeVarInt(size.getZ());
	}
}
