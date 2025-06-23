package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberImBuilder;
import imgui.ImGui;
import net.minecraft.world.phys.Vec3;

public record DynamicWorldVector(WorldNumber x, WorldNumber y, WorldNumber z) implements WorldVector {
	public static final SimpleRegistryType<DynamicWorldVector> TYPE = SimpleRegistryType.dynamic("dynamic", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("x").forGetter(DynamicWorldVector::x),
		WorldNumber.CODEC.fieldOf("y").forGetter(DynamicWorldVector::y),
		WorldNumber.CODEC.fieldOf("z").forGetter(DynamicWorldVector::z)
	).apply(instance, DynamicWorldVector::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, DynamicWorldVector::x,
		WorldNumber.STREAM_CODEC, DynamicWorldVector::y,
		WorldNumber.STREAM_CODEC, DynamicWorldVector::z,
		DynamicWorldVector::new
	));

	public static class Builder implements WorldVectorImBuilder {
		public static final ImBuilderHolder<WorldVector> TYPE = new ImBuilderHolder<>("Dynamic", Builder::new);

		public final ImBuilder<WorldNumber> x = WorldNumberImBuilder.create(0D);
		public final ImBuilder<WorldNumber> y = WorldNumberImBuilder.create(0D);
		public final ImBuilder<WorldNumber> z = WorldNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			ImGui.alignTextToFramePadding();
			ImGui.text("X");
			ImGui.sameLine();
			ImGui.pushID("###x");
			update = update.or(x.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("Y");
			ImGui.sameLine();
			ImGui.pushID("###y");
			update = update.or(y.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("Z");
			ImGui.sameLine();
			ImGui.pushID("###z");
			update = update.or(z.imgui(graphics));
			ImGui.popID();

			return update;
		}

		@Override
		public boolean isValid() {
			return x.isValid() && y.isValid() && z.isValid();
		}

		@Override
		public WorldVector build() {
			return new DynamicWorldVector(x.build(), y.build(), z.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Vec3 get(WorldNumberContext ctx) {
		var px = x.get(ctx);
		var py = y.get(ctx);
		var pz = z.get(ctx);

		if (px == null || py == null || pz == null) {
			return null;
		}

		return KMath.vec3(px, py, pz);
	}
}
