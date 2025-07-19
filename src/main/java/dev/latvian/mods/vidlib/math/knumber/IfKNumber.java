package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.util.MiscUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record IfKNumber(
	KNumber ifValue,
	Comparison comparison,
	KNumber testValue,
	Optional<KNumber> thenValue,
	Optional<KNumber> elseValue
) implements KNumber {
	public static final SimpleRegistryType<IfKNumber> TYPE = SimpleRegistryType.dynamic("if", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("if").forGetter(IfKNumber::ifValue),
		Comparison.DATA_TYPE.codec().optionalFieldOf("comparison", Comparison.NOT_EQUALS).forGetter(IfKNumber::comparison),
		KNumber.CODEC.optionalFieldOf("value", KNumber.ZERO).forGetter(IfKNumber::testValue),
		KNumber.CODEC.optionalFieldOf("then").forGetter(IfKNumber::thenValue),
		KNumber.CODEC.optionalFieldOf("else").forGetter(IfKNumber::elseValue)
	).apply(instance, IfKNumber::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, IfKNumber::ifValue,
		Comparison.DATA_TYPE.streamCodec().optional(Comparison.NOT_EQUALS), IfKNumber::comparison,
		KNumber.STREAM_CODEC.optional(KNumber.ZERO), IfKNumber::testValue,
		KNumber.STREAM_CODEC.optional(), IfKNumber::thenValue,
		KNumber.STREAM_CODEC.optional(), IfKNumber::elseValue,
		IfKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("If", Builder::new);

		public final ImBuilder<KNumber> ifValue = KNumberImBuilder.create(1D);
		public final Comparison[] comparison = {Comparison.NOT_EQUALS};
		public final ImBuilder<KNumber> testValue = KNumberImBuilder.create(0D);
		public final ImBoolean thenValueEnabled = new ImBoolean(true);
		public final ImBuilder<KNumber> thenValue = KNumberImBuilder.create(0D);
		public final ImBoolean elseValueEnabled = new ImBoolean(false);
		public final ImBuilder<KNumber> elseValue = KNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("If", !ifValue.isValid());
			ImGui.sameLine();
			ImGui.pushID("###if");
			update = update.or(ifValue.imgui(graphics));
			ImGui.popID();

			update = update.or(graphics.combo("###comparison", "", comparison, MiscUtils.COMPARISONS));

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("Value", !testValue.isValid());
			ImGui.sameLine();
			ImGui.pushID("###value");
			update = update.or(testValue.imgui(graphics));
			ImGui.popID();

			boolean thenInvalid = thenValueEnabled.get() && !thenValue.isValid();

			if (thenInvalid) {
				graphics.pushStack();
				graphics.setErrorText();
			}

			update = update.or(ImGui.checkbox("Then###then-enabled", thenValueEnabled));

			if (thenInvalid) {
				graphics.popStack();
			}

			if (thenValueEnabled.get()) {
				ImGui.sameLine();
				ImGui.pushID("###then");
				update = update.or(thenValue.imgui(graphics));
				ImGui.popID();
			}

			boolean elseInvalid = elseValueEnabled.get() && !elseValue.isValid();

			if (elseInvalid) {
				graphics.pushStack();
				graphics.setErrorText();
			}

			update = update.or(ImGui.checkbox("Else###else-enabled", elseValueEnabled));

			if (elseInvalid) {
				graphics.popStack();
			}

			if (elseValueEnabled.get()) {
				ImGui.sameLine();
				ImGui.pushID("###else");
				update = update.or(elseValue.imgui(graphics));
				ImGui.popID();
			}

			return update;
		}

		@Override
		public boolean isValid() {
			return ifValue.isValid() && testValue.isValid() && (!thenValueEnabled.get() || thenValue.isValid()) && (!elseValueEnabled.get() || elseValue.isValid());
		}

		@Override
		public KNumber build() {
			return new IfKNumber(
				ifValue.build(),
				comparison[0],
				testValue.build(),
				thenValueEnabled.get() ? Optional.of(thenValue.build()) : Optional.empty(),
				elseValueEnabled.get() ? Optional.of(elseValue.build()) : Optional.empty()
			);
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var i = ifValue.get(ctx);

		if (i == null) {
			return null;
		}

		var t = testValue.get(ctx);

		if (t == null) {
			return null;
		}

		if (comparison.test(i, t)) {
			if (thenValue.isPresent()) {
				return thenValue.get().get(ctx);
			}
		} else {
			if (elseValue.isPresent()) {
				return elseValue.get().get(ctx);
			}
		}

		return null;
	}
}
