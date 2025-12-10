package dev.latvian.mods.vidlib.feature.feature;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.feature.session.LoginData;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ClientFeaturesPayload(FeatureSet featureSet) implements SimplePacketPayload {
	@AutoPacket(to = AutoPacket.To.SERVER, stage = AutoPacket.Stage.CONFIG)
	public static final VidLibPacketType<ClientFeaturesPayload> TYPE = VidLibPacketType.internal("client_features", FeatureSet.STREAM_CODEC.map(ClientFeaturesPayload::new, ClientFeaturesPayload::featureSet));

	public record ClientFeatureSetLoginData(FeatureSet featureSet) implements LoginData {
		@Override
		public void transfer(ServerGamePacketListener connection, ServerSessionData data) {
			data.clientFeatureSet = featureSet;
		}
	}

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.addLoginData(new ClientFeatureSetLoginData(featureSet));
		ctx.finishTask(ServerFeaturesPayload.CONFIG_TASK.type());
	}
}
