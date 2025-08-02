package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

public class EnumImBuilder<E> implements ImBuilder<E> {
	public static final ImBuilderSupplier<Mirror> MIRROR_SUPPLIER = () -> new EnumImBuilder<>(Mirror[]::new, Mirror.values());
	public static final ImBuilderSupplier<Rotation> BLOCK_ROTATION_SUPPLIER = () -> new EnumImBuilder<>(Rotation[]::new, Rotation.values());
	public static final ImBuilderSupplier<LiquidSettings> LIQUID_SETTINGS_SUPPLIER = () -> new EnumImBuilder<>(LiquidSettings[]::new, LiquidSettings.values());
	public static final ImBuilderSupplier<InteractionHand> HAND_SUPPLIER = () -> new EnumImBuilder<>(InteractionHand[]::new, InteractionHand.values());

	public final E[] value;
	public final List<E> options;

	public static EnumImBuilder<Easing> easing() {
		var builder = new EnumImBuilder<>(Easing.ARRAY_FACTORY, Easing.VALUES);
		builder.set(Easing.LINEAR);
		return builder;
	}

	public EnumImBuilder(IntFunction<E[]> arrayConstructor, List<E> options) {
		this.value = arrayConstructor.apply(1);
		this.options = options;
		this.value[0] = options.getFirst();
	}

	public EnumImBuilder(IntFunction<E[]> arrayConstructor, E[] options) {
		this(arrayConstructor, Arrays.asList(options));
	}

	@Override
	public void set(E v) {
		value[0] = v;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return graphics.combo("###enum", "Select...", value, options);
	}

	@Override
	public E build() {
		return value[0];
	}
}
