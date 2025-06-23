package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record IfWorldNumber(
	WorldNumber ifValue,
	Comparison comparison,
	WorldNumber testValue,
	Optional<WorldNumber> thenValue,
	Optional<WorldNumber> elseValue
) implements WorldNumber {
	public static final SimpleRegistryType<IfWorldNumber> TYPE = SimpleRegistryType.dynamic("if", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("if").forGetter(IfWorldNumber::ifValue),
		Comparison.DATA_TYPE.codec().optionalFieldOf("comparison", Comparison.NOT_EQUALS).forGetter(IfWorldNumber::comparison),
		WorldNumber.CODEC.optionalFieldOf("value", FixedWorldNumber.ZERO.instance()).forGetter(IfWorldNumber::testValue),
		WorldNumber.CODEC.optionalFieldOf("then").forGetter(IfWorldNumber::thenValue),
		WorldNumber.CODEC.optionalFieldOf("else").forGetter(IfWorldNumber::elseValue)
	).apply(instance, IfWorldNumber::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, IfWorldNumber::ifValue,
		Comparison.DATA_TYPE.streamCodec().optional(Comparison.NOT_EQUALS), IfWorldNumber::comparison,
		WorldNumber.STREAM_CODEC.optional(FixedWorldNumber.ZERO.instance()), IfWorldNumber::testValue,
		WorldNumber.STREAM_CODEC.optional(), IfWorldNumber::thenValue,
		WorldNumber.STREAM_CODEC.optional(), IfWorldNumber::elseValue,
		IfWorldNumber::new
	));

	public static class Builder implements WorldNumberImBuilder {
		public static final Comparison[] COMPARISONS = Comparison.values();
		public static final ImBuilderHolder<WorldNumber> TYPE = new ImBuilderHolder<>("If", Builder::new);

		public final ImBuilder<WorldNumber> ifValue = WorldNumberImBuilder.create(1D);
		public final Comparison[] comparison = {Comparison.NOT_EQUALS};
		public final ImBuilder<WorldNumber> testValue = WorldNumberImBuilder.create(0D);
		public final ImBoolean thenValueEnabled = new ImBoolean(true);
		public final ImBuilder<WorldNumber> thenValue = WorldNumberImBuilder.create(0D);
		public final ImBoolean elseValueEnabled = new ImBoolean(false);
		public final ImBuilder<WorldNumber> elseValue = WorldNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("If", !ifValue.isValid());
			ImGui.sameLine();
			ImGui.pushID("###if");
			update = update.or(ifValue.imgui(graphics));
			ImGui.popID();

			update = update.or(graphics.combo("###comparison", "", comparison, COMPARISONS));

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("Value", !testValue.isValid());
			ImGui.sameLine();
			ImGui.pushID("###value");
			update = update.or(testValue.imgui(graphics));
			ImGui.popID();

			boolean thenInvalid = thenValueEnabled.get() && !thenValue.isValid();

			if (thenInvalid) {
				graphics.pushStack();
				graphics.setRedText();
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
				graphics.setRedText();
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
		public WorldNumber build() {
			return new IfWorldNumber(
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
	public Double get(WorldNumberContext ctx) {
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
