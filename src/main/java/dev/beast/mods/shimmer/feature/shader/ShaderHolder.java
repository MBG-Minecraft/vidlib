package dev.beast.mods.shimmer.feature.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShaderHolder implements Consumer<ShaderInstance>, Supplier<ShaderInstance> {
	public final ResourceLocation id;
	public final VertexFormat format;
	public final List<Consumer<ShaderHolder>> reloadListeners;
	public ShaderInstance instance;

	public ShaderHolder(ResourceLocation id, VertexFormat format) {
		this.id = id;
		this.format = format;
		this.reloadListeners = new ArrayList<>(0);
	}

	public void register(RegisterShadersEvent event) throws IOException {
		event.registerShader(new ShaderInstance(event.getResourceProvider(), id, format), this);
	}

	public void addListener(Consumer<ShaderHolder> listener) {
		reloadListeners.add(listener);
	}

	@Override
	public void accept(ShaderInstance newInstance) {
		instance = newInstance;

		for (var listener : reloadListeners) {
			listener.accept(this);
		}
	}

	@Override
	public ShaderInstance get() {
		return instance;
	}

	public AbstractUniform get(String name) {
		return instance.safeGetUniform(name);
	}
}
