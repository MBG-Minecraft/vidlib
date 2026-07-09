package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.klib.util.UnitSupplier;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnumImBuilder<E> implements ImBuilder<E> {
	public static final ImBuilderType<Mirror> MIRROR_TYPE = typeOf(Mirror.values(), Mirror.NONE);
	public static final ImBuilderType<Rotation> BLOCK_ROTATION_TYPE = typeOf(Rotation.values(), Rotation.NONE);
	public static final ImBuilderType<LiquidSettings> LIQUID_SETTINGS_TYPE = typeOf(LiquidSettings.values(), LiquidSettings.IGNORE_WATERLOGGING);
	public static final ImBuilderType<InteractionHand> HAND_TYPE = typeOf(InteractionHand.values(), InteractionHand.MAIN_HAND);
	public static final ImBuilderType<Comparison> COMPARISON_TYPE = typeOf(Comparison.VALUES, Comparison.NOT_EQUALS);

	public static <E> Builder<E> of(Supplier<? extends Iterable<? extends E>> options) {
		return new Builder<>(options);
	}

	public static <E> Builder<E> of(Iterable<? extends E> options) {
		return of(new UnitSupplier<>(options));
	}

	public static <E> Builder<E> of(E[] options) {
		return of(new UnitSupplier<>(Arrays.asList(options)));
	}

	public static <E> ImBuilderType<E> typeOf(E[] options, @Nullable E defaultValue) {
		return of(options).defaultValue(defaultValue).buildType();
	}

	public static <E> ImBuilderType<E> typeOf(Iterable<E> options, @Nullable E defaultValue) {
		return of(options).defaultValue(defaultValue).buildType();
	}

	public static class Builder<E> {
		private final Supplier<? extends Iterable<? extends E>> options;
		private Supplier<@Nullable E> defaultValue;
		private Function<E, String> nameGetter;
		private String nullName;

		private Builder(Supplier<? extends Iterable<? extends E>> options) {
			this.options = options;
			this.defaultValue = null;
			this.nameGetter = null;
			this.nullName = "Not Set";
		}

		public Builder<E> defaultValue(@Nullable Supplier<@Nullable E> defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public Builder<E> defaultValue(@Nullable E defaultValue) {
			this.defaultValue = defaultValue == null ? null : new UnitSupplier<>(defaultValue);
			return this;
		}

		public Builder<E> nameGetter(Function<E, String> nameGetter) {
			this.nameGetter = nameGetter;
			return this;
		}

		public Builder<E> nullName(String nullName) {
			this.nullName = nullName;
			return this;
		}

		public EnumImBuilder<E> build() {
			return new EnumImBuilder<>(options.get(), defaultValue == null ? null : defaultValue.get(), nameGetter, nullName);
		}

		public ImBuilderType<E> buildType() {
			return this::build;
		}
	}

	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final Iterable<? extends E> options;
	public final Object[] value;
	public boolean allowNull;
	public final Function<E, String> nameGetter;
	public final String nullName;

	public EnumImBuilder(Iterable<? extends E> options, @Nullable E defaultValue, Function<E, String> nameGetter, String nullName) {
		this.options = options;
		this.value = new Object[]{defaultValue};
		this.allowNull = defaultValue == null;
		this.nameGetter = nameGetter;
		this.nullName = nullName;
	}

	@Override
	public void set(E v) {
		value[0] = v;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return graphics.combo("###enum", value, allowNull ? nullName : "", options, nameGetter, SEARCH);
	}

	@Override
	public boolean isValid() {
		return allowNull || value[0] != null;
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
