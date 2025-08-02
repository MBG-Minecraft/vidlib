package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.data.DataType;
import net.neoforged.bus.api.Event;

import java.util.function.BiConsumer;

public class DataTypeImBuilderEvent extends Event {
	private final BiConsumer<DataType<?>, ImBuilderSupplier<?>> callback;

	public DataTypeImBuilderEvent(BiConsumer<DataType<?>, ImBuilderSupplier<?>> callback) {
		this.callback = callback;
	}

	public <T> void register(DataType<T> type, ImBuilderSupplier<T> builder) {
		callback.accept(type, builder);
	}
}
