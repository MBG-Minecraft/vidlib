package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.StreamCodec;

public enum RemoveAllParticlesPayload implements SimplePacketPayload {
	INSTANCE;

	@AutoPacket
	public static final VidLibPacketType<RemoveAllParticlesPayload> TYPE = VidLibPacketType.internal("remove_all_particles", StreamCodec.unit(INSTANCE));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().removeAllParticles();
	}
}
