package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.StreamCodec;

public enum RemoveAllParticlesPayload implements SimplePayload {
	INSTANCE;

	@AutoPacket
	public static final SimplePayloadType<RemoveAllParticlesPayload> TYPE = SimplePayloadType.vidlib("remove_all_particles", StreamCodec.unit(INSTANCE));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().removeAllParticles();
	}
}
