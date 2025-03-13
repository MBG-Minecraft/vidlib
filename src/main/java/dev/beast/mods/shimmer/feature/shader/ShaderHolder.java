package dev.beast.mods.shimmer.feature.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import dev.beast.mods.shimmer.util.WithCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShaderHolder implements WithCache, Supplier<CompiledShaderProgram> {
	public final ShaderProgram program;
	public final List<Consumer<CompiledShaderProgram>> reloadListeners;
	public CompiledShaderProgram compiled;

	public ShaderHolder(ShaderProgram program) {
		this.program = program;
		this.reloadListeners = new ArrayList<>(0);
	}

	public ShaderHolder(ResourceLocation id, VertexFormat format) {
		this(new ShaderProgram(id, format, ShaderDefines.EMPTY));
	}

	public void register(RegisterShadersEvent event) throws IOException {
		event.registerShader(program);
	}

	public void addListener(Consumer<CompiledShaderProgram> listener) {
		reloadListeners.add(listener);
	}

	@Override
	public void clearCache() {
		compiled = null;
	}

	@Override
	public CompiledShaderProgram get() {
		if (compiled == null) {
			compiled = Minecraft.getInstance().getShaderManager().getProgram(program);

			if (compiled != null) {
				for (var listener : reloadListeners) {
					listener.accept(compiled);
				}
			}
		}

		return compiled;
	}
}
