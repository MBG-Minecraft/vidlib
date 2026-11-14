package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScreenEffectRenderingRegistryEvent extends Event implements IModBusEvent {
	public <SE extends ScreenEffect> void register(DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type, Consumer<ScreenEffectClientInfo<SE>> builder) {
		var renderer = new ScreenEffectClientInfo<SE>(type);
		builder.accept(renderer);
		type.setProperty(ScreenEffect.PROPERTY_EXTRACTOR, renderer);
	}

	public void register(DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type, ImIcon icon, String displayName) {
		register(type, builder -> builder.setDisplayInfo(icon, displayName));
	}

	public <SE extends ScreenEffect, S extends ScreenEffectState> void register(DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type, ImIcon icon, String displayName, Supplier<S> stateFactory, ScreenEffectStateExtractor<SE, S> stateExtractor) {
		register(type, builder -> {
			builder.setDisplayInfo(icon, displayName);
			builder.setState(Cast.to(stateFactory), Cast.to(stateExtractor));
		});
	}
}
