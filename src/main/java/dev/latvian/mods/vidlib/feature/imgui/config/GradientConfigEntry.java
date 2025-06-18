package dev.latvian.mods.vidlib.feature.imgui.config;

import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.CompoundGradient;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.color.PairGradient;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;

import java.util.ArrayList;
import java.util.List;

public class GradientConfigEntry extends ConfigEntry<Gradient> {
	public final List<float[]> data;

	public GradientConfigEntry(String label, DataKey<Gradient> key) {
		super(label, key);
		this.data = new ArrayList<>(1);
	}

	@Override
	public Gradient get() {
		var list = new ArrayList<Color>();

		for (var d : data) {
			list.add(Color.of(1F, d[0], d[1], d[2]));
		}

		return new CompoundGradient(list).resolve();
	}

	@Override
	public void set(Gradient value) {
		data.clear();

		if (value instanceof PairGradient pair) {
			var start = pair.start().get(0F);
			var end = pair.end().get(0F);

			data.add(new float[]{start.redf(), start.greenf(), start.bluef()});
			data.add(new float[]{end.redf(), end.greenf(), end.bluef()});
		} else if (value instanceof CompoundGradient compound) {
			for (var c : compound.children()) {
				var col = c.get(0F);
				data.add(new float[]{col.redf(), col.greenf(), col.bluef()});
			}
		} else {
			var col = value.get(0F);
			data.add(new float[]{col.redf(), col.greenf(), col.bluef()});
		}
	}

	@Override
	public boolean imguiValue() {
		boolean changed = false;

		for (int i = 0; i < data.size(); i++) {
			if (i > 0) {
				ImGui.sameLine();
			}

			var d = data.get(i);
			changed = ImGui.colorEdit3(id + "-" + i, d, ImGuiColorEditFlags.NoInputs) | changed;
		}

		return changed;
	}

	@Override
	public String json(Gradient value) {
		try {
			return Gradient.CODEC.encodeStart(JsonOps.INSTANCE, value).getOrThrow().toString();
		} catch (Exception ignore) {
			return "transparent";
		}
	}
}
