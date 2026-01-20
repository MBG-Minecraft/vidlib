package dev.latvian.mods.vidlib.feature.feature;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;

public record ClientFeaturesPayload(FeatureSet featureSet) implements SimplePacketPayload {
	@AutoPacket(to = AutoPacket.To.SERVER, stage = AutoPacket.Stage.CONFIG)
	public static final VidLibPacketType<ClientFeaturesPayload> TYPE = VidLibPacketType.internal("client_features", FeatureSet.STREAM_CODEC.map(ClientFeaturesPayload::new, ClientFeaturesPayload::featureSet));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.sessionData() instanceof ServerSessionData sessionData) {
			sessionData.clientFeatureSet = featureSet;
		}

		ctx.finishTask(ServerFeaturesPayload.CONFIG_TASK.type());
	}
}
