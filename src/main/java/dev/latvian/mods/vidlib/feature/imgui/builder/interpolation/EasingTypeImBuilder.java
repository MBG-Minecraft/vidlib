package dev.latvian.mods.vidlib.feature.imgui.builder.interpolation;

import dev.latvian.mods.klib.interpolation.Interpolation;
import dev.latvian.mods.klib.interpolation.WrappedEasingType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.util.EasingType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * ImBuilder for the vanilla {@link EasingType} that KLib 2603 extends with its own
 * {@link Interpolation} registry ({@link WrappedEasingType}).
 */
public class EasingTypeImBuilder implements ImBuilder<EasingType> {
	public static final ImBuilderType<EasingType> TYPE = EasingTypeImBuilder::new;

	private static final String CUSTOM = "Custom";
	private static final EasingType DEFAULT = EasingType.LINEAR;
	private static final String[] NAMES = {
		"constant",
		"linear",
		"in_back",
		"in_bounce",
		"in_circ",
		"in_cubic",
		"in_elastic",
		"in_expo",
		"in_quad",
		"in_quart",
		"in_quint",
		"in_sine",
		"in_out_back",
		"in_out_bounce",
		"in_out_circ",
		"in_out_cubic",
		"in_out_elastic",
		"in_out_expo",
		"in_out_quad",
		"in_out_quart",
		"in_out_quint",
		"in_out_sine",
		"out_back",
		"out_bounce",
		"out_circ",
		"out_cubic",
		"out_elastic",
		"out_expo",
		"out_quad",
		"out_quart",
		"out_quint",
		"out_sine"
	};

	private static List<EasingType> builtIn() {
		return List.of(
			EasingType.CONSTANT,
			EasingType.LINEAR,
			EasingType.IN_BACK,
			EasingType.IN_BOUNCE,
			EasingType.IN_CIRC,
			EasingType.IN_CUBIC,
			EasingType.IN_ELASTIC,
			EasingType.IN_EXPO,
			EasingType.IN_QUAD,
			EasingType.IN_QUART,
			EasingType.IN_QUINT,
			EasingType.IN_SINE,
			EasingType.IN_OUT_BACK,
			EasingType.IN_OUT_BOUNCE,
			EasingType.IN_OUT_CIRC,
			EasingType.IN_OUT_CUBIC,
			EasingType.IN_OUT_ELASTIC,
			EasingType.IN_OUT_EXPO,
			EasingType.IN_OUT_QUAD,
			EasingType.IN_OUT_QUART,
			EasingType.IN_OUT_QUINT,
			EasingType.IN_OUT_SINE,
			EasingType.OUT_BACK,
			EasingType.OUT_BOUNCE,
			EasingType.OUT_CIRC,
			EasingType.OUT_CUBIC,
			EasingType.OUT_ELASTIC,
			EasingType.OUT_EXPO,
			EasingType.OUT_QUAD,
			EasingType.OUT_QUART,
			EasingType.OUT_QUINT,
			EasingType.OUT_SINE
		);
	}

	private EasingType value = DEFAULT;
	private boolean custom = false;
	public final ImBuilder<Ref<Interpolation>> interpolation = InterpolationImBuilder.create();
	private final ImInt current = new ImInt(1);

	public EasingTypeImBuilder() {
	}

	@Override
	public void set(@Nullable EasingType value) {
		this.value = value == null ? DEFAULT : value;
		this.custom = value instanceof WrappedEasingType;
		this.interpolation.set(this.custom && value instanceof WrappedEasingType w ? w.ref() : Interpolation.linear());
		var builtIn = builtIn();
		int index = builtIn.indexOf(this.value);
		this.current.set(index < 0 ? 1 : index + 1);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var builtIn = builtIn();
		var items = new String[builtIn.size() + 1];
		items[0] = CUSTOM;

		for (int i = 0; i < builtIn.size(); i++) {
			items[i + 1] = NAMES[i];
		}

		ImGui.combo("###easing", current, items);
		boolean custom = current.get() == 0;
		this.custom = custom;
		this.value = custom ? this.value : builtIn.get(current.get() - 1);

		if (!custom) {
			return ImUpdate.NONE;
		}

		ImGui.sameLine();
		return interpolation.imguiKey(graphics, "", "easing-custom");
	}

	@Override
	public boolean isValid() {
		return !custom || interpolation.isValid();
	}

	@Override
	public EasingType build() {
		if (custom && interpolation.isValid()) {
			var ref = interpolation.build();
			return ref == null ? value : ref.value().toEasingType();
		}

		return value;
	}
}
