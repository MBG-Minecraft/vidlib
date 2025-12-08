package dev.latvian.mods.vidlib.feature.feature;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public record ClientFeaturesPayload(FeatureSet featureSet) implements SimplePacketPayload {
	@AutoPacket(value = AutoPacket.To.SERVER, stage = AutoPacket.Stage.CONFIG)
	public static final VidLibPacketType<ClientFeaturesPayload> TYPE = VidLibPacketType.internalConfig("client_features", FeatureSet.STREAM_CODEC.map(ClientFeaturesPayload::new, ClientFeaturesPayload::featureSet));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.parent().listener() instanceof ServerConfigurationPacketListenerImpl listener) {
			// listener.getOwner()
			listener.finishCurrentTask(ServerFeaturesConfigurationTask.TYPE);
		}
	}
}
