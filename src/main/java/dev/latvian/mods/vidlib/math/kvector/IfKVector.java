package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import dev.latvian.mods.vidlib.util.MiscUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record IfKVector(
	KNumber ifValue,
	Comparison comparison,
	KNumber testValue,
	Optional<KVector> thenValue,
	Optional<KVector> elseValue
) implements KVector {
	public static final SimpleRegistryType<IfKVector> TYPE = SimpleRegistryType.dynamic("if", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("if").forGetter(IfKVector::ifValue),
		Comparison.DATA_TYPE.codec().optionalFieldOf("comparison", Comparison.NOT_EQUALS).forGetter(IfKVector::comparison),
		KNumber.CODEC.optionalFieldOf("value", KNumber.ZERO).forGetter(IfKVector::testValue),
		KVector.CODEC.optionalFieldOf("then").forGetter(IfKVector::thenValue),
		KVector.CODEC.optionalFieldOf("else").forGetter(IfKVector::elseValue)
	).apply(instance, IfKVector::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, IfKVector::ifValue,
		Comparison.DATA_TYPE.streamCodec().optional(Comparison.NOT_EQUALS), IfKVector::comparison,
		KNumber.STREAM_CODEC.optional(KNumber.ZERO), IfKVector::testValue,
		KVector.STREAM_CODEC.optional(), IfKVector::thenValue,
		KVector.STREAM_CODEC.optional(), IfKVector::elseValue,
		IfKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("If", Builder::new);

		public final ImBuilder<KNumber> ifValue = KNumberImBuilder.create(1D);
		public final Comparison[] comparison = {Comparison.NOT_EQUALS};
		public final ImBuilder<KNumber> testValue = KNumberImBuilder.create(0D);
		public final ImBoolean thenValueEnabled = new ImBoolean(true);
		public final ImBuilder<KVector> thenValue = KVectorImBuilder.create();
		public final ImBoolean elseValueEnabled = new ImBoolean(false);
		public final ImBuilder<KVector> elseValue = KVectorImBuilder.create();

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			ImGui.alignTextToFramePadding();
			ImGui.text("If");
			ImGui.sameLine();
			ImGui.pushID("###if");
			update = update.or(ifValue.imgui(graphics));
			ImGui.popID();

			update = update.or(graphics.combo("###comparison", "", comparison, MiscUtils.COMPARISONS));

			ImGui.alignTextToFramePadding();
			ImGui.text("Value");
			ImGui.sameLine();
			ImGui.pushID("###value");
			update = update.or(testValue.imgui(graphics));
			ImGui.popID();

			update = update.or(ImGui.checkbox("Then###then-enabled", thenValueEnabled));

			if (thenValueEnabled.get()) {
				ImGui.sameLine();
				ImGui.pushID("###then");
				update = update.or(thenValue.imgui(graphics));
				ImGui.popID();
			}

			update = update.or(ImGui.checkbox("Else###else-enabled", elseValueEnabled));

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
		public KVector build() {
			return new IfKVector(
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
}
