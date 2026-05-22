package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import net.minecraft.core.Registry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public class EnumImBuilder<E> implements ImBuilder<E> {
	public static final ImBuilderType<Mirror> MIRROR_TYPE = () -> new EnumImBuilder<>(Mirror.values(), Mirror.NONE);
	public static final ImBuilderType<Rotation> BLOCK_ROTATION_TYPE = () -> new EnumImBuilder<>(Rotation.values(), Rotation.NONE);
	public static final ImBuilderType<LiquidSettings> LIQUID_SETTINGS_TYPE = () -> new EnumImBuilder<>(LiquidSettings.values(), LiquidSettings.IGNORE_WATERLOGGING);
	public static final ImBuilderType<InteractionHand> HAND_TYPE = () -> new EnumImBuilder<>(InteractionHand.values(), InteractionHand.MAIN_HAND);

	public static <T> EnumImBuilder<T> ofRegistry(Registry<T> registry, @Nullable T defaultValue, Function<T, String> nameGetter) {
		var builder = new EnumImBuilder<>(registry.stream().toList(), defaultValue);
		builder.nameGetter = nameGetter;
		return builder;
	}

	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final Collection<E> options;
	public final Object[] value;
	public boolean allowNull;
	public Function<E, String> nameGetter;

	public EnumImBuilder(Collection<E> options, @Nullable E defaultValue) {
		this.options = options;
		this.value = new Object[]{defaultValue};
		this.allowNull = defaultValue == null;
		this.nameGetter = (Function) KLibCodecs.DEFAULT_NAME_GETTER;
	}

	public EnumImBuilder(E[] options, E defaultValue) {
		this(Arrays.asList(options), defaultValue);
	}

	public EnumImBuilder(Collection<E> options) {
		this(options, null);
	}

	public EnumImBuilder(E[] options) {
		this(Arrays.asList(options));
	}

	public EnumImBuilder<E> withNameGetter(Function<E, String> nameGetter) {
		var b = new EnumImBuilder<>(options, build());
		b.nameGetter = nameGetter;
		return b;
	}

	@Override
	public void set(E v) {
		value[0] = v;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return graphics.combo("###enum", value, allowNull ? "Not Set" : "", options, nameGetter, SEARCH);
	}

	@Override
	public boolean isValid() {
		return value[0] != null;
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
