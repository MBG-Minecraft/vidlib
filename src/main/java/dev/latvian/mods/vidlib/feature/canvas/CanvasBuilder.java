package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CanvasBuilder {
	Consumer<Minecraft> tickCallback = null;
	Consumer<Minecraft> drawSetupCallback = null;
	Consumer<Minecraft> drawCallback = null;
	List<CanvasSampler> samplers = new ArrayList<>();
	List<CanvasUniform> uniforms = new ArrayList<>();

	public void setTickCallback(Consumer<Minecraft> callback) {
		this.tickCallback = callback;
	}

	public void setDrawSetupCallback(Consumer<Minecraft> callback) {
		this.drawSetupCallback = callback;
	}

	public void setDrawCallback(Consumer<Minecraft> callback) {
		this.drawCallback = callback;
	}

	public void addSampler(String name, Supplier<GpuTexture> valueSupplier) {
		this.samplers.add(new CanvasSampler(name, valueSupplier));
	}

	public void addUniform(CanvasUniform uniform) {
		this.uniforms.add(uniform);
	}
}
