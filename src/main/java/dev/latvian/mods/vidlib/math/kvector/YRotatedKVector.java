package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import imgui.ImGui;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public record YRotatedKVector(KVector vector, KNumber angle) implements KVector {
	public static final SimpleRegistryType<YRotatedKVector> TYPE = SimpleRegistryType.dynamic("y_rotated", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KVector.CODEC.fieldOf("vector").forGetter(YRotatedKVector::vector),
		KNumber.CODEC.fieldOf("angle").forGetter(YRotatedKVector::angle)
	).apply(instance, YRotatedKVector::new)), CompositeStreamCodec.of(
		KVector.STREAM_CODEC, YRotatedKVector::vector,
		KNumber.STREAM_CODEC, YRotatedKVector::angle,
		YRotatedKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("Y-Rotated", Builder::new);

		public final ImBuilder<KVector> vector = KVectorImBuilder.create();
		public final ImBuilder<KNumber> angle = KNumberImBuilder.create(5D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.pushItemWidth(-1F);

			ImGui.alignTextToFramePadding();
			ImGui.text("Vector");
			ImGui.sameLine();
			ImGui.pushID("###vector");
			update = update.or(vector.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("Angle");
			ImGui.sameLine();
			ImGui.pushID("###angle");
			update = update.or(angle.imgui(graphics));
			ImGui.popID();

			ImGui.popItemWidth();
			return update;
		}

		@Override
		public boolean isValid() {
			return vector.isValid() && angle.isValid();
		}

		@Override
		public KVector build() {
			return new YRotatedKVector(vector.build(), angle.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
		var vector = this.vector.get(ctx);

		if (vector == null) {
			return null;
		}

		var angle = this.angle.get(ctx);

		if (angle == null) {
			return null;
		}

		var vec = new Vector3d(vector.x, vector.y, vector.z);
		vec.rotateY(Math.toRadians(angle));
		return KMath.vec3(vec.x, vec.y, vec.z);
	}
}
