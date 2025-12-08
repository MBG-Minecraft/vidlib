package dev.latvian.mods.vidlib.feature.feature;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;

import java.util.function.Consumer;

public record ServerFeaturesConfigurationTask(ServerConfigurationPacketListener listener) implements ConfigurationTask {
	public static final Type TYPE = new Type(VidLib.id("server_features_configuration_task"));

	@Override
	public Type type() {
		return TYPE;
	}

	@Override
	public void start(Consumer<Packet<?>> consumer) {
		if (listener.hasChannel(ServerFeaturesPayload.TYPE.type())) {
			consumer.accept(new ServerFeaturesPayload(FeatureSet.SERVER_FEATURES.get()).toConfigS2C());
		}

		// listener.finishCurrentTask(TYPE);
	}
}
