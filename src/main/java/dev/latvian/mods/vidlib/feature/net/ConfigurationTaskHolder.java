package dev.latvian.mods.vidlib.feature.net;

import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record ConfigurationTaskHolder(Identifier id, ConfigurationTask.Type type, BiConsumer<NetworkChannelInfo, Registry> registry) {
	public interface Registry {
		void register(VLConfigurationTask task);
	}

	public ConfigurationTaskHolder(Identifier id, BiConsumer<NetworkChannelInfo, Registry> registry) {
		this(id, new ConfigurationTask.Type(id), registry);
	}

	public void register(ServerConfigurationPacketListener listener, NetworkChannelInfo channelInfo, Consumer<ConfigurationTask> taskRegistry) {
		registry.accept(channelInfo, task -> taskRegistry.accept(new VLConfigurationTask.WrappedConfigurationTask(type, listener, channelInfo, task)));
	}
}
