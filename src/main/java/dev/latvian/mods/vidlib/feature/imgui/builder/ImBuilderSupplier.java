package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.Lazy;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;
import java.util.function.Supplier;

@FunctionalInterface
public interface ImBuilderSupplier<T> extends Supplier<ImBuilder<? extends T>> {
	Lazy<Map<DataType<?>, ImBuilderSupplier<?>>> BY_DATA_TYPE = Lazy.identityMap(map -> NeoForge.EVENT_BUS.post(new DataTypeImBuilderEvent(map::put)));
}
