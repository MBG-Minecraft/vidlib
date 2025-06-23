package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImNumberType;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImDouble;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;

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
		public static final ImBuilderHolder<WorldNumber> TYPE = new ImBuilderHolder<>("Number", Builder::new, true);

		public final ImDouble data;

		public Builder() {
			this.data = new ImDouble();
		}

		public Builder(double value) {
			this.data = new ImDouble(value);
		}

		@Override
		public void set(WorldNumber value) {
			if (value instanceof FixedWorldNumber(Double n)) {
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
		public WorldNumber build() {
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
}
