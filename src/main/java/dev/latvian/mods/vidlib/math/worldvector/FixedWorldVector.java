package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.Vector3dImBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;

public record FixedWorldVector(Vec3 vec) implements WorldVector {
	public static final SimpleRegistryType.Unit<FixedWorldVector> ZERO = SimpleRegistryType.unit("zero", new FixedWorldVector(Vec3.ZERO));
	public static final SimpleRegistryType.Unit<FixedWorldVector> ONE = SimpleRegistryType.unit("one", new FixedWorldVector(new Vec3(1D, 1D, 1D)));

	public static final SimpleRegistryType<FixedWorldVector> TYPE = SimpleRegistryType.dynamic("fixed", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MCCodecs.VEC3.fieldOf("vec").forGetter(FixedWorldVector::vec)
	).apply(instance, FixedWorldVector::new)), MCStreamCodecs.VEC3.map(FixedWorldVector::new, FixedWorldVector::vec));

	public static class Builder implements WorldVectorImBuilder {
		public static final ImBuilderHolder<WorldVector> TYPE = new ImBuilderHolder<>("Vector", Builder::new, true);

		public final Vector3dImBuilder builder = new Vector3dImBuilder();

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return builder.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return builder.isValid();
		}

		@Override
		public WorldVector build() {
			return new FixedWorldVector(builder.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return vec.x == 0D && vec.y == 0D && vec.z == 0D ? ZERO : vec.x == 1D && vec.y == 1D && vec.z == 1D ? ONE : TYPE;
	}

	@Override
	public Vec3 get(WorldNumberContext ctx) {
		return vec;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}
}
