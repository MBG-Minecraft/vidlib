package dev.latvian.mods.vidlib.feature.feature;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.net.ConfigurationTaskHolder;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VLConfigurationTask;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record ServerFeaturesPayload(FeatureSet featureSet) implements SimplePacketPayload {
	@AutoPacket(stage = AutoPacket.Stage.CONFIG)
	public static final VidLibPacketType<ServerFeaturesPayload> TYPE = VidLibPacketType.internal("server_features", FeatureSet.STREAM_CODEC.map(ServerFeaturesPayload::new, ServerFeaturesPayload::featureSet));

	@AutoRegister
	public static final ConfigurationTaskHolder CONFIG_TASK = new ConfigurationTaskHolder(VidLib.id("features"), (channelInfo, registry) -> registry.register(ServerFeaturesPayload::config));

	private static void config(VLConfigurationTask.Context ctx) {
		if (ctx.channelInfo().hasChannel(TYPE)) {
			ctx.send(new ServerFeaturesPayload(FeatureSet.SERVER_FEATURES.get()).toConfigS2C());
		}
	}

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handleAsync(Context ctx) {
		handleClient(ctx);
	}

	private void handleClient(Context ctx) {
		FeatureSet.REMOTE_SERVER_FEATURES = featureSet;
		ctx.listener().send(new ClientFeaturesPayload(FeatureSet.CLIENT_FEATURES.get()).toConfigC2S());
	}
}
