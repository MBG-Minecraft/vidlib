package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.type.ImBoolean;
import net.minecraft.network.codec.ByteBufCodecs;
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
		Comparison.DATA_TYPE.streamCodec(), IfKNumber::comparison,
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ZERO), IfKNumber::testValue,
		ByteBufCodecs.optional(KNumber.STREAM_CODEC), IfKNumber::thenValue,
		ByteBufCodecs.optional(KNumber.STREAM_CODEC), IfKNumber::elseValue,
		IfKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("If", Builder::new);

		public final ImBuilder<KNumber> ifValue = KNumberImBuilder.create(1D);
		public final ImBuilder<Comparison> comparison = new EnumImBuilder<>(Comparison.ARRAY_FACTORY, Comparison.VALUES, Comparison.NOT_EQUALS);
		public final ImBuilder<KNumber> testValue = KNumberImBuilder.create(0D);
		public final ImBoolean thenValueEnabled = new ImBoolean(true);
		public final ImBuilder<KNumber> thenValue = KNumberImBuilder.create(0D);
		public final ImBoolean elseValueEnabled = new ImBoolean(false);
		public final ImBuilder<KNumber> elseValue = KNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(ifValue.imguiKey(graphics, "If", "if"));
			update = update.or(comparison.imguiKey(graphics, "Comparison", "comparison"));
			update = update.or(testValue.imguiKey(graphics, "Value", "value"));
			update = update.or(testValue.imguiOptionalKey(graphics, thenValueEnabled, "Then", "then"));
			update = update.or(testValue.imguiOptionalKey(graphics, elseValueEnabled, "Else", "else"));
			return update;
		}

		@Override
		public boolean isValid() {
			return ifValue.isValid() && comparison.isValid() && testValue.isValid() && (!thenValueEnabled.get() || thenValue.isValid()) && (!elseValueEnabled.get() || elseValue.isValid());
		}

		@Override
		public KNumber build() {
			return new IfKNumber(
				ifValue.build(),
				comparison.build(),
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
