package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public record YRotatedKVector(KVector vector, KNumber angle) implements KVector, ImBuilderWrapper.BuilderSupplier {
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
		public void set(KVector value) {
			if (value instanceof YRotatedKVector v) {
				vector.set(v.vector);
				angle.set(v.angle);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(vector.imguiKey(graphics, "Vector", "vector"));
			update = update.or(angle.imguiKey(graphics, "Angle", "angle"));
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

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
