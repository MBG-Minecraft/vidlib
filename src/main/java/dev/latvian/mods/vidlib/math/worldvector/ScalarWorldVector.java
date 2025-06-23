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
import org.jetbrains.annotations.Nullable;

public record ScalarWorldVector(WorldNumber number) implements WorldVector {
	public static final SimpleRegistryType<ScalarWorldVector> TYPE = SimpleRegistryType.dynamic("scalar", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("number").forGetter(ScalarWorldVector::number)
	).apply(instance, ScalarWorldVector::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, ScalarWorldVector::number,
		ScalarWorldVector::new
	));

	public static class Builder implements WorldVectorImBuilder {
		public static final ImBuilderHolder<WorldVector> TYPE = new ImBuilderHolder<>("Scalar (n, n, n)", Builder::new);

		public final ImBuilder<WorldNumber> number = WorldNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			ImGui.pushID("###number");
			update = update.or(number.imgui(graphics));
			ImGui.popID();

			return update;
		}

		@Override
		public boolean isValid() {
			return number.isValid();
		}

		@Override
		public WorldVector build() {
			return new ScalarWorldVector(number.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var n = number.get(ctx);

		if (n == null) {
			return null;
		}

		return KMath.vec3(n, n, n);
	}
}
