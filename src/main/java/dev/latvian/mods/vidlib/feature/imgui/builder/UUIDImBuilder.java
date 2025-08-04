package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.Util;

import java.util.UUID;

public class UUIDImBuilder implements ImBuilder<UUID> {
	public static final ImBuilderSupplier<UUID> SUPPLIER = UUIDImBuilder::new;

	public final ImString value;
	private UUID uuid;

	public UUIDImBuilder() {
		this.value = ImGuiUtils.resizableString(Util.NIL_UUID.toString());
		this.uuid = null;
	}

	@Override
	public void set(UUID v) {
		value.set(v.toString());
		uuid = v;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		if (ImGui.inputText("###uuid", value)) {
			try {
				uuid = value.isEmpty() ? null : UndashedUuid.fromStringLenient(value.get());
			} catch (Exception e) {
				uuid = null;
			}
		}

		return ImUpdate.itemEdit();
	}

	@Override
	public boolean isValid() {
		return uuid != null;
	}

	@Override
	public UUID build() {
		return uuid;
	}
}
