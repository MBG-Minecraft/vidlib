package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import imgui.ImGui;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ScaledWorldVector(WorldVector a, WorldVector b) implements WorldVector {
	public static final SimpleRegistryType<ScaledWorldVector> TYPE = SimpleRegistryType.dynamic("scaled", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldVector.CODEC.fieldOf("a").forGetter(ScaledWorldVector::a),
		WorldVector.CODEC.fieldOf("b").forGetter(ScaledWorldVector::b)
	).apply(instance, ScaledWorldVector::new)), CompositeStreamCodec.of(
		WorldVector.STREAM_CODEC, ScaledWorldVector::a,
		WorldVector.STREAM_CODEC, ScaledWorldVector::b,
		ScaledWorldVector::new
	));

	public static class Builder implements WorldVectorImBuilder {
		public static final ImBuilderHolder<WorldVector> TYPE = new ImBuilderHolder<>("Scaled (a * b)", Builder::new);

		public final ImBuilder<WorldVector> a = WorldVectorImBuilder.create();
		public final ImBuilder<WorldVector> b = WorldVectorImBuilder.create();

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			ImGui.alignTextToFramePadding();
			ImGui.text("A");
			ImGui.sameLine();
			ImGui.pushID("###a");
			update = update.or(a.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("B");
			ImGui.sameLine();
			ImGui.pushID("###b");
			update = update.or(b.imgui(graphics));
			ImGui.popID();

			return update;
		}

		@Override
		public boolean isValid() {
			return a.isValid() && b.isValid();
		}

		@Override
		public WorldVector build() {
			return new ScaledWorldVector(a.build(), b.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var a = this.a.get(ctx);
		var b = this.b.get(ctx);

		if (a == null || b == null) {
			return null;
		}

		return new Vec3(a.x * b.x, a.y * b.y, a.z * b.z);
	}
}
