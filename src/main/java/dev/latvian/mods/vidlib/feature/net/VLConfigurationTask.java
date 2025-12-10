package dev.latvian.mods.vidlib.feature.net;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;

import java.util.function.Consumer;

@FunctionalInterface
public interface VLConfigurationTask {
	record WrappedConfigurationTask(ConfigurationTask.Type type, ServerConfigurationPacketListener listener, NetworkChannelInfo channelInfo, VLConfigurationTask task) implements ConfigurationTask {
		@Override
		public void start(Consumer<Packet<?>> send) {
			task.start(new Context(listener, channelInfo, send));
		}
	}

	record Context(ServerConfigurationPacketListener listener, NetworkChannelInfo channelInfo, Consumer<Packet<?>> send) {
		public void send(Packet<?> packet) {
			send.accept(packet);
		}
	}

	void start(Context ctx);
}
