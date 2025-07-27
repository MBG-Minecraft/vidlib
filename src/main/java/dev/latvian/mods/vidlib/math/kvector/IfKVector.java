package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import imgui.type.ImBoolean;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record IfKVector(
	KNumber ifValue,
	Comparison comparison,
	KNumber testValue,
	Optional<KVector> thenValue,
	Optional<KVector> elseValue
) implements KVector, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<IfKVector> TYPE = SimpleRegistryType.dynamic("if", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("if").forGetter(IfKVector::ifValue),
		Comparison.DATA_TYPE.codec().optionalFieldOf("comparison", Comparison.NOT_EQUALS).forGetter(IfKVector::comparison),
		KNumber.CODEC.optionalFieldOf("value", KNumber.ZERO).forGetter(IfKVector::testValue),
		KVector.CODEC.optionalFieldOf("then").forGetter(IfKVector::thenValue),
		KVector.CODEC.optionalFieldOf("else").forGetter(IfKVector::elseValue)
	).apply(instance, IfKVector::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, IfKVector::ifValue,
		Comparison.DATA_TYPE.streamCodec(), IfKVector::comparison,
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ZERO), IfKVector::testValue,
		ByteBufCodecs.optional(KVector.STREAM_CODEC), IfKVector::thenValue,
		ByteBufCodecs.optional(KVector.STREAM_CODEC), IfKVector::elseValue,
		IfKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("If", Builder::new);

		public final ImBuilder<KNumber> ifValue = KNumberImBuilder.create(1D);
		public final ImBuilder<Comparison> comparison = new EnumImBuilder<>(Comparison.ARRAY_FACTORY, Comparison.VALUES, Comparison.NOT_EQUALS);
		public final ImBuilder<KNumber> testValue = KNumberImBuilder.create(0D);
		public final ImBoolean thenValueEnabled = new ImBoolean(true);
		public final ImBuilder<KVector> thenValue = KVectorImBuilder.create();
		public final ImBoolean elseValueEnabled = new ImBoolean(false);
		public final ImBuilder<KVector> elseValue = KVectorImBuilder.create();

		@Override
		public void set(KVector value) {
			if (value instanceof IfKVector v) {
				ifValue.set(v.ifValue);
				comparison.set(v.comparison);
				testValue.set(v.testValue);
				thenValueEnabled.set(v.thenValue.isPresent());
				thenValue.set(v.thenValue.orElse(KVector.ZERO));
				elseValueEnabled.set(v.elseValue.isPresent());
				elseValue.set(v.elseValue.orElse(KVector.ZERO));
			}
		}

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
		public KVector build() {
			return new IfKVector(
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
	public Vec3 get(KNumberContext ctx) {
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

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
