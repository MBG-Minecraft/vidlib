package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

public record Atan2KNumber(KNumber x, KNumber y) implements KNumber {
	public static final SimpleRegistryType<Atan2KNumber> TYPE = SimpleRegistryType.dynamic("atan2", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("x", KNumber.ZERO).forGetter(Atan2KNumber::x),
		KNumber.CODEC.optionalFieldOf("y", KNumber.ONE).forGetter(Atan2KNumber::y)
	).apply(instance, Atan2KNumber::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC.optional(KNumber.ZERO), Atan2KNumber::x,
		KNumber.STREAM_CODEC.optional(KNumber.ONE), Atan2KNumber::y,
		Atan2KNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("Atan2", Builder::new);

		public final ImBuilder<KNumber> x = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> y = KNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.pushItemWidth(-1F);

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("X", !x.isValid());
			ImGui.sameLine();
			ImGui.pushID("###x");
			update = update.or(x.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("Y", !y.isValid());
			ImGui.sameLine();
			ImGui.pushID("###y");
			update = update.or(y.imgui(graphics));
			ImGui.popID();

			ImGui.popItemWidth();
			return update;
		}

		@Override
		public boolean isValid() {
			return x.isValid() && y.isValid();
		}

		@Override
		public KNumber build() {
			return new Atan2KNumber(x.build(), y.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var x = this.x.get(ctx);
		var y = this.y.get(ctx);

		if (x == null || y == null) {
			return null;
		}

		return Math.toDegrees(Math.atan2(y, x));
	}
}
