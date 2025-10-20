package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImNumberType;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePin;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePinType;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImDouble;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;

import java.util.List;

public record FixedKNumber(Double number) implements KNumber, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<FixedKNumber> TYPE = SimpleRegistryType.dynamic("fixed", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.DOUBLE.fieldOf("number").forGetter(FixedKNumber::number)
	).apply(instance, KNumber::of)), ByteBufCodecs.DOUBLE.map(KNumber::of, FixedKNumber::number));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = ImBuilderHolder.ofDefault("Number", Builder::new);

		public final ImDouble number;

		public Builder() {
			this.number = new ImDouble();
		}

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KNumber value) {
			if (value instanceof FixedKNumber n) {
				number.set(n.number);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var range = graphics.getNumberRange();

			if (graphics.getNumberType() == ImNumberType.INT) {
				ImGuiUtils.INT.set(Mth.floor(number.get()));

				if (range != null) {
					ImGui.sliderInt("###value", ImGuiUtils.INT.getData(), Mth.floor(range.min()), Mth.ceil(range.max()));
				} else {
					ImGui.dragInt("###value", ImGuiUtils.INT.getData(), 0.05F);
				}

				number.set(ImGuiUtils.INT.get());
			} else if (range != null) {
				ImGuiUtils.FLOAT.set((float) number.get());
				ImGui.sliderFloat("###value", ImGuiUtils.FLOAT.getData(), range.min(), range.max());
				number.set(ImGuiUtils.FLOAT.get());
			} else {
				ImGuiUtils.FLOAT.set((float) number.get());
				ImGui.dragFloat("###value", ImGuiUtils.FLOAT.getData(), 0.05F);
				number.set(ImGuiUtils.FLOAT.get());
			}

			return ImUpdate.itemEdit();
		}

		@Override
		public boolean isValid() {
			return !Double.isNaN(number.get());
		}

		@Override
		public KNumber build() {
			return KNumber.of(number.get());
		}

		@Override
		public List<NodePin> getNodePins() {
			return NodePinType.NUMBER.singleOutput;
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

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
