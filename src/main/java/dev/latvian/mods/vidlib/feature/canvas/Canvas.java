package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.renderpearl.api.commands.RenderPass;
import dev.latvian.mods.klib.util.Lazy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public record Canvas(Identifier postEffect, BooleanSupplier active, List<CustomPostPassUniforms> uniforms) {
	public static final Lazy<List<Canvas>> ALL = Lazy.of(() -> {
		var list = new ArrayList<Canvas>();
		ModLoader.postEvent(new CanvasRegistryEvent(list::add));
		return List.copyOf(list);
	});

	public static final Lazy<Map<String, CustomPostPassUniforms>> UNIFORMS = ALL.map(canvas -> {
		var map = new Object2ObjectOpenHashMap<String, CustomPostPassUniforms>();

		for (var c : canvas) {
			for (var u : c.uniforms) {
				map.put(u.name, u);
			}
		}

		return map;
	});

	public static boolean replaceUniforms(RenderPass instance, String key) {
		var r = UNIFORMS.get().get(key);
		var b = r == null ? null : r.getBuffer();

		if (b != null) {
			instance.setUniform(key, b);
			return true;
		} else {
			return false;
		}
	}

	public static void appendPostEffects(List<Identifier> list) {
		for (var canvas : ALL.get()) {
			if (canvas.active.getAsBoolean()) {
				list.add(canvas.postEffect);
			}
		}
	}

	public Canvas(Identifier postEffect, BooleanSupplier active, String uniformName, List<UniformSupplier> uniformSuppliers) {
		this(postEffect, active, List.of(new CustomPostPassUniforms(uniformName, uniformSuppliers)));
	}

	public void update() {
		for (var it : uniforms) {
			it.update();
		}
	}
}
