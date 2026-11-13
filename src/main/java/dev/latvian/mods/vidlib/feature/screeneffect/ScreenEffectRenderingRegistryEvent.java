package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.client.CustomPostPassUniforms;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.function.Consumer;

public class ScreenEffectRenderingRegistryEvent extends Event implements IModBusEvent {
	public void register(DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type, Consumer<ScreenEffectRenderer> builder) {
		var renderer = new ScreenEffectRenderer(type);
		builder.accept(renderer);
		type.setProperty(ScreenEffect.PROPERTY_RENDERER, renderer);

		for (var uniforms : renderer.uniforms) {
			CustomPostPassUniforms.MAP.put(uniforms.name, uniforms);
		}
	}
}
