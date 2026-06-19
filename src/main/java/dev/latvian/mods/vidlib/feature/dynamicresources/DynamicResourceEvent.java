package dev.latvian.mods.vidlib.feature.dynamicresources;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.function.Consumer;

public abstract class DynamicResourceEvent extends Event implements IModBusEvent {
	private final PackType type;
	private final Consumer<Identifier> callback;

	public DynamicResourceEvent(PackType type, Consumer<Identifier> callback) {
		this.type = type;
		this.callback = callback;
	}

	public PackType getType() {
		return this.type;
	}

	public void register(Identifier id) {
		this.callback.accept(id);
	}

	public static class Assets extends DynamicResourceEvent {
		public Assets(Consumer<Identifier> callback) {
			super(PackType.CLIENT_RESOURCES, callback);
		}
	}

	public static class Data extends DynamicResourceEvent {
		public Data(Consumer<Identifier> callback) {
			super(PackType.SERVER_DATA, callback);
		}
	}
}
