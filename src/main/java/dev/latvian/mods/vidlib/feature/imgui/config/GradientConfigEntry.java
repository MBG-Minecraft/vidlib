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
			list.add(Color.of(d[3], d[0], d[1], d[2]));
		}

		return new CompoundGradient(list).resolve();
	}

	@Override
	public void set(Gradient value) {
		data.clear();

		if (value instanceof PairGradient pair) {
			var start = pair.start().get(0F);
			var end = pair.end().get(0F);

			data.add(new float[]{start.redf(), start.greenf(), start.bluef(), start.alphaf()});
			data.add(new float[]{end.redf(), end.greenf(), end.bluef(), end.alphaf()});
		} else if (value instanceof CompoundGradient compound) {
			for (var c : compound.children()) {
				var col = c.get(0F);
				data.add(new float[]{col.redf(), col.greenf(), col.bluef(), col.alphaf()});
			}
		} else {
			var col = value.get(0F);
			data.add(new float[]{col.redf(), col.greenf(), col.bluef(), col.alphaf()});
		}
	}

	@Override
	public Update imguiValue() {
		Update update = Update.NONE;

		for (int i = 0; i < data.size(); i++) {
			if (i > 0) {
				ImGui.sameLine();
			}

			var d = data.get(i);
			ImGui.colorEdit4(id + "-" + i, d, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.NoLabel);
			update = update.or(Update.itemEdit());
		}

		return update;
	}

	@Override
	public String json(Gradient value) {
		try {
			return Gradient.CODEC.encodeStart(JsonOps.INSTANCE, value).getOrThrow().toString();
		} catch (Exception ignore) {
			return "\"transparent\"";
		}
	}
}
