package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImDouble;
import net.minecraft.network.codec.ByteBufCodecs;

public record FixedWorldNumber(Double number) implements WorldNumber {
	public static final SimpleRegistryType.Unit<FixedWorldNumber> ZERO = SimpleRegistryType.unit("zero", new FixedWorldNumber(0D));
	public static final SimpleRegistryType.Unit<FixedWorldNumber> ONE = SimpleRegistryType.unit("one", new FixedWorldNumber(1D));

	public static FixedWorldNumber of(double number) {
		if (number == 0D) {
			return ZERO.instance();
		} else if (number == 1D) {
			return ONE.instance();
		} else {
			return new FixedWorldNumber(number);
		}
	}

	public static final SimpleRegistryType<FixedWorldNumber> TYPE = SimpleRegistryType.dynamic("fixed", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.DOUBLE.fieldOf("number").forGetter(FixedWorldNumber::number)
	).apply(instance, FixedWorldNumber::of)), ByteBufCodecs.DOUBLE.map(FixedWorldNumber::of, FixedWorldNumber::number));

	public static class Builder implements WorldNumberImBuilder {
		public static final ImBuilderHolder<WorldNumber> TYPE = new ImBuilderHolder<>("Fixed", Builder::new, true);

		public final ImDouble data;

		public Builder() {
			this.data = new ImDouble();
		}

		public Builder(double value) {
			this.data = new ImDouble(value);
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.inputDouble("###value", data);
			return ImUpdate.itemEdit();
		}

		@Override
		public FixedWorldNumber build() {
			return of(data.get());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return number == 0D ? ZERO : number == 1D ? ONE : TYPE;
	}

	@Override
	public Double get(WorldNumberContext ctx) {
		return number;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public Builder createBuilder() {
		var builder = new Builder();
		builder.data.set(number);
		return builder;
	}
}
