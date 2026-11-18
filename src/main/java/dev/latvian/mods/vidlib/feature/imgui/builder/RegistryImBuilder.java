package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.UnitType;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import org.jetbrains.annotations.Nullable;

public class RegistryImBuilder<E> implements ImBuilder<Ref<E>> {
	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final CustomRegistry<?, E> registry;
	public final Object[] value;
	public boolean allowNull;

	public RegistryImBuilder(CustomRegistry<?, E> registry, @Nullable Ref<E> defaultValue) {
		this.registry = registry;
		this.value = new Object[]{defaultValue};
		this.allowNull = defaultValue == null;
	}

	public RegistryImBuilder(CustomRegistry<?, E> registry) {
		this(registry, null);
	}

	@Override
	public void set(Ref<E> v) {
		value[0] = v;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		if (value[0] == null || value[0] instanceof UnitType<?, ?>) {
			update = update.or(graphics.combo("###unit", value, allowNull ? "Not Set" : "", registry, Ref::key, SEARCH));
		}

		return update;
	}

	@Override
	public boolean isValid() {
		return allowNull || value[0] != null;
	}

	@Override
	public Ref<E> build() {
		return (Ref<E>) value[0];
	}
}
