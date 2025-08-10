package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.flag.ImGuiComboFlags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class EnumImBuilder<E> implements ImBuilder<E> {
	public static final ImBuilderType<Easing> EASING_TYPE = () -> new EnumImBuilder<>(Easing.VALUES, Easing.LINEAR);
	public static final ImBuilderType<Mirror> MIRROR_TYPE = () -> new EnumImBuilder<>(Mirror.values(), Mirror.NONE);
	public static final ImBuilderType<Rotation> BLOCK_ROTATION_TYPE = () -> new EnumImBuilder<>(Rotation.values(), Rotation.NONE);
	public static final ImBuilderType<LiquidSettings> LIQUID_SETTINGS_TYPE = () -> new EnumImBuilder<>(LiquidSettings.values(), LiquidSettings.IGNORE_WATERLOGGING);
	public static final ImBuilderType<InteractionHand> HAND_TYPE = () -> new EnumImBuilder<>(InteractionHand.values(), InteractionHand.MAIN_HAND);

	public final List<E> options;
	public final Object[] value;
	public Function<E, String> nameGetter;

	public EnumImBuilder(List<E> options, E defaultValue) {
		this.options = options;
		this.value = new Object[]{defaultValue};
		this.nameGetter = (Function) KLibCodecs.DEFAULT_NAME_GETTER;
	}

	public EnumImBuilder(E[] options, E defaultValue) {
		this(Arrays.asList(options), defaultValue);
	}

	public EnumImBuilder(List<E> options) {
		this(options, options.getFirst());
	}

	public EnumImBuilder(E[] options) {
		this(Arrays.asList(options));
	}

	public EnumImBuilder<E> withNameGetter(Function<E, String> nameGetter) {
		var b = new EnumImBuilder<E>(options, build());
		b.nameGetter = nameGetter;
		return b;
	}

	@Override
	public void set(E v) {
		value[0] = v;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return graphics.combo("###enum", "Select...", value, options, nameGetter, ImGuiComboFlags.None);
	}

	@Override
	public E build() {
		return (E) value[0];
	}

	@Override
	public <O> String toString(DynamicOps<O> ops, E value) {
		return KLibCodecs.DEFAULT_NAME_GETTER.apply(Cast.to(value));
	}
}
