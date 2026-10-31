package dev.latvian.mods.vidlib.feature.feature;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.net.ConfigurationTaskHolder;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.net.VLConfigurationTask;

public record ServerFeaturesPayload(FeatureSet featureSet) implements SimplePayload {
	@AutoPacket(stage = AutoPacket.Stage.CONFIG)
	public static final SimplePayloadType<ServerFeaturesPayload> TYPE = SimplePayloadType.vidlib("server_features", FeatureSet.STREAM_CODEC.map(ServerFeaturesPayload::new, ServerFeaturesPayload::featureSet));

	@AutoRegister
	public static final ConfigurationTaskHolder CONFIG_TASK = new ConfigurationTaskHolder(ID.vidlib("features"), (channelInfo, registry) -> registry.register(ServerFeaturesPayload::config));

	private static void config(VLConfigurationTask.Context ctx) {
		if (ctx.channelInfo().hasChannel(TYPE)) {
			ctx.send(new ServerFeaturesPayload(FeatureSet.SERVER_FEATURES.get()).toS2CPacket());
		}
	}

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handleAsync(Context ctx) {
		handleClient(ctx);
	}

	private void handleClient(Context ctx) {
		FeatureSet.REMOTE_SERVER_FEATURES = featureSet;
		ctx.send(new ClientFeaturesPayload(FeatureSet.CLIENT_FEATURES.get()).toC2SPacket());
	}
}
