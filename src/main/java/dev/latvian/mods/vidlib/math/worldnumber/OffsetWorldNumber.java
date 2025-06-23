package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

public record OffsetWorldNumber(WorldNumber a, WorldNumber b) implements WorldNumber {
	public static final SimpleRegistryType<OffsetWorldNumber> TYPE = SimpleRegistryType.dynamic("offset", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("a").forGetter(OffsetWorldNumber::a),
		WorldNumber.CODEC.fieldOf("b").forGetter(OffsetWorldNumber::b)
	).apply(instance, OffsetWorldNumber::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, OffsetWorldNumber::a,
		WorldNumber.STREAM_CODEC, OffsetWorldNumber::b,
		OffsetWorldNumber::new
	));

	public static class Builder implements WorldNumberImBuilder {
		public static final ImBuilderHolder<WorldNumber> TYPE = new ImBuilderHolder<>("Offset (a + b)", Builder::new);

		public final ImBuilder<WorldNumber> a = WorldNumberImBuilder.create(0D);
		public final ImBuilder<WorldNumber> b = WorldNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.pushItemWidth(-1F);

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("A", !a.isValid());
			ImGui.sameLine();
			ImGui.pushID("###a");
			update = update.or(a.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("B", !b.isValid());
			ImGui.sameLine();
			ImGui.pushID("###b");
			update = update.or(b.imgui(graphics));
			ImGui.popID();

			ImGui.popItemWidth();
			return update;
		}

		@Override
		public boolean isValid() {
			return a.isValid() && b.isValid();
		}

		@Override
		public WorldNumber build() {
			return new OffsetWorldNumber(a.build(), b.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(WorldNumberContext ctx) {
		var a = this.a.get(ctx);
		var b = this.b.get(ctx);

		if (a == null || b == null) {
			return null;
		}

		return a + b;
	}
}
