package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.FixedWorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberImBuilder;
import dev.latvian.mods.vidlib.util.MiscUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record IfWorldVector(
	WorldNumber ifValue,
	Comparison comparison,
	WorldNumber testValue,
	Optional<WorldVector> thenValue,
	Optional<WorldVector> elseValue
) implements WorldVector {
	public static final SimpleRegistryType<IfWorldVector> TYPE = SimpleRegistryType.dynamic("if", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("if").forGetter(IfWorldVector::ifValue),
		Comparison.DATA_TYPE.codec().optionalFieldOf("comparison", Comparison.NOT_EQUALS).forGetter(IfWorldVector::comparison),
		WorldNumber.CODEC.optionalFieldOf("value", FixedWorldNumber.ZERO.instance()).forGetter(IfWorldVector::testValue),
		WorldVector.CODEC.optionalFieldOf("then").forGetter(IfWorldVector::thenValue),
		WorldVector.CODEC.optionalFieldOf("else").forGetter(IfWorldVector::elseValue)
	).apply(instance, IfWorldVector::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, IfWorldVector::ifValue,
		Comparison.DATA_TYPE.streamCodec().optional(Comparison.NOT_EQUALS), IfWorldVector::comparison,
		WorldNumber.STREAM_CODEC.optional(FixedWorldNumber.ZERO.instance()), IfWorldVector::testValue,
		WorldVector.STREAM_CODEC.optional(), IfWorldVector::thenValue,
		WorldVector.STREAM_CODEC.optional(), IfWorldVector::elseValue,
		IfWorldVector::new
	));

	public static class Builder implements WorldVectorImBuilder {
		public static final ImBuilderHolder<WorldVector> TYPE = new ImBuilderHolder<>("If", Builder::new);

		public final ImBuilder<WorldNumber> ifValue = WorldNumberImBuilder.create(1D);
		public final Comparison[] comparison = {Comparison.NOT_EQUALS};
		public final ImBuilder<WorldNumber> testValue = WorldNumberImBuilder.create(0D);
		public final ImBoolean thenValueEnabled = new ImBoolean(true);
		public final ImBuilder<WorldVector> thenValue = WorldVectorImBuilder.create();
		public final ImBoolean elseValueEnabled = new ImBoolean(false);
		public final ImBuilder<WorldVector> elseValue = WorldVectorImBuilder.create();

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
		public WorldVector build() {
			return new IfWorldVector(
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
	public Vec3 get(WorldNumberContext ctx) {
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
