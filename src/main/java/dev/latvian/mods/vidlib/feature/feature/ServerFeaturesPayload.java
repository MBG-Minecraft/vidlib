package dev.latvian.mods.vidlib.feature.feature;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerFeaturesPayload(FeatureSet featureSet) implements SimplePacketPayload {
	@AutoPacket(stage = AutoPacket.Stage.CONFIG)
	public static final VidLibPacketType<ServerFeaturesPayload> TYPE = VidLibPacketType.internalConfig("server_features", FeatureSet.STREAM_CODEC.map(ServerFeaturesPayload::new, ServerFeaturesPayload::featureSet));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handleAsync(IPayloadContext payloadContext, long uid, long remoteGameTime) {
		handleClient(payloadContext);
	}

	private void handleClient(IPayloadContext payloadContext) {
		FeatureSet.REMOTE_SERVER_FEATURES = featureSet;
		payloadContext.listener().send(new ClientFeaturesPayload(FeatureSet.CLIENT_FEATURES.get()).toConfigC2S());
	}
}
