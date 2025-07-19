package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImNumberType;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImDouble;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;

public record FixedKNumber(Double number) implements KNumber {
	public static final SimpleRegistryType<FixedKNumber> TYPE = SimpleRegistryType.dynamic("fixed", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.DOUBLE.fieldOf("number").forGetter(FixedKNumber::number)
	).apply(instance, KNumber::of)), ByteBufCodecs.DOUBLE.map(KNumber::of, FixedKNumber::number));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("Number", Builder::new, true);

		public final ImDouble data;

		public Builder() {
			this.data = new ImDouble();
		}

		public Builder(double value) {
			this.data = new ImDouble(value);
		}

		@Override
		public void set(KNumber value) {
			if (value instanceof FixedKNumber(Double n)) {
				data.set(n);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var range = graphics.getNumberRange();

			if (graphics.getNumberType() == ImNumberType.INT) {
				ImGuiUtils.INT.set(Mth.floor(data.get()));

				if (range != null) {
					ImGui.sliderInt("###value", ImGuiUtils.INT.getData(), Mth.floor(range.min()), Mth.ceil(range.max()));
				} else {
					ImGui.inputInt("###value", ImGuiUtils.INT);
				}

				data.set(ImGuiUtils.INT.get());
			} else if (range != null) {
				ImGuiUtils.FLOAT.set((float) data.get());
				ImGui.sliderFloat("###value", ImGuiUtils.FLOAT.getData(), range.min(), range.max());
				data.set(ImGuiUtils.FLOAT.get());
			} else {
				ImGui.inputDouble("###value", data);
			}

			return ImUpdate.itemEdit();
		}

		@Override
		public boolean isValid() {
			return !Double.isNaN(data.get());
		}

		@Override
		public KNumber build() {
			return KNumber.of(data.get());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return number == 0D ? KNumber.ZERO_TYPE : number == 1D ? KNumber.ONE_TYPE : TYPE;
	}

	@Override
	public Double get(KNumberContext ctx) {
		return number;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public KNumber offset(KNumber other) {
		if (other instanceof FixedKNumber n) {
			return KNumber.of(number + n.number);
		}

		return KNumber.super.offset(other);
	}

	@Override
	public KNumber scale(KNumber other) {
		if (other instanceof FixedKNumber n) {
			return KNumber.of(number * n.number);
		}

		return KNumber.super.scale(other);
	}
}
