package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Range;
import dev.latvian.mods.vidlib.feature.config.BooleanConfigValue;
import dev.latvian.mods.vidlib.feature.config.ConfigValue;
import dev.latvian.mods.vidlib.feature.config.FloatConfigValue;
import dev.latvian.mods.vidlib.feature.config.RangeConfigValue;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.util.JsonCodecReloadListener;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public class PhysicsParticleData {
	public static final float DEFAULT_DENSITY = 4F;
	public static final Range DEFAULT_LIFESPAN = Range.of(80F, 120F);
	public static final Range DEFAULT_SCALE = Range.of(1F, 2F);
	public static final Range DEFAULT_POWER = Range.of(0.2F, 2F);
	public static final Range DEFAULT_SPREAD = Range.of(0F, 0.3F);
	public static final float DEFAULT_INERTIA = 0.96F;
	public static final float DEFAULT_GRAVITY = 0.036F;
	public static final Range DEFAULT_SPEED = Range.ONE;
	public static final float DEFAULT_DIRECTION = 0F;
	public static final float DEFAULT_TILT = 0F;
	public static final Range DEFAULT_SECTION = Range.of(0F, 360F);
	public static final boolean DEFAULT_IGNORE_BLOCK_DENSITY = false;

	public static final PhysicsParticleData DEFAULT = new PhysicsParticleData();

	public static final Codec<PhysicsParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.FLOAT.optionalFieldOf("density", DEFAULT_DENSITY).forGetter(p -> p.density),
		Range.CODEC.optionalFieldOf("lifespan", DEFAULT_LIFESPAN).forGetter(p -> p.lifespan),
		Range.CODEC.optionalFieldOf("scale", DEFAULT_SCALE).forGetter(p -> p.scale),
		Range.CODEC.optionalFieldOf("power", DEFAULT_POWER).forGetter(p -> p.power),
		Range.CODEC.optionalFieldOf("spread", DEFAULT_SPREAD).forGetter(p -> p.spread),
		Codec.FLOAT.optionalFieldOf("inertia", DEFAULT_INERTIA).forGetter(p -> p.inertia),
		Codec.FLOAT.optionalFieldOf("gravity", DEFAULT_GRAVITY).forGetter(p -> p.gravity),
		Range.CODEC.optionalFieldOf("speed", DEFAULT_SPEED).forGetter(p -> p.speed),
		Codec.FLOAT.optionalFieldOf("direction", DEFAULT_DIRECTION).forGetter(p -> p.direction),
		Codec.FLOAT.optionalFieldOf("tilt", DEFAULT_TILT).forGetter(p -> p.tilt),
		Range.CODEC.optionalFieldOf("section", DEFAULT_SECTION).forGetter(p -> p.section),
		Codec.BOOL.optionalFieldOf("ignore_block_density", DEFAULT_IGNORE_BLOCK_DENSITY).forGetter(p -> p.ignoreBlockDensity)
	).apply(instance, PhysicsParticleData::new));

	public static final StreamCodec<ByteBuf, PhysicsParticleData> STREAM_CODEC = CompositeStreamCodec.of(
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, DEFAULT_DENSITY), p -> p.density,
		KLibStreamCodecs.optional(Range.STREAM_CODEC, DEFAULT_LIFESPAN), p -> p.lifespan,
		KLibStreamCodecs.optional(Range.STREAM_CODEC, DEFAULT_SCALE), p -> p.scale,
		KLibStreamCodecs.optional(Range.STREAM_CODEC, DEFAULT_POWER), p -> p.power,
		KLibStreamCodecs.optional(Range.STREAM_CODEC, DEFAULT_SPREAD), p -> p.spread,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, DEFAULT_INERTIA), p -> p.inertia,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, DEFAULT_GRAVITY), p -> p.gravity,
		KLibStreamCodecs.optional(Range.STREAM_CODEC, DEFAULT_SPEED), p -> p.speed,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, DEFAULT_DIRECTION), p -> p.direction,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, DEFAULT_TILT), p -> p.tilt,
		KLibStreamCodecs.optional(Range.STREAM_CODEC, DEFAULT_SECTION), p -> p.section,
		ByteBufCodecs.BOOL, p -> p.ignoreBlockDensity,
		PhysicsParticleData::new
	);

	public static final DataType<PhysicsParticleData> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, PhysicsParticleData.class);

	public static final List<ConfigValue<PhysicsParticleData, ?>> CONFIG = List.of(
		new FloatConfigValue<>("Density", Range.of(0F, 1000F), false, data -> data.density, (data, v) -> data.density = v),
		new RangeConfigValue<>("Lifespan", Range.of(1F, 300F), true, data -> data.lifespan, (data, v) -> data.lifespan = v),
		new RangeConfigValue<>("Scale", Range.of(0F, 10F), true, data -> data.scale, (data, v) -> data.scale = v),
		new RangeConfigValue<>("Power", null, false, data -> data.power, (data, v) -> data.power = v),
		new RangeConfigValue<>("Spread", Range.FULL, true, data -> data.spread, (data, v) -> data.spread = v),
		new FloatConfigValue<>("Inertia", Range.FULL, true, data -> data.inertia, (data, v) -> data.inertia = v),
		new FloatConfigValue<>("Gravity", null, false, data -> data.gravity, (data, v) -> data.gravity = v),
		new RangeConfigValue<>("Speed", Range.of(0.1F, 10F), true, data -> data.speed, (data, v) -> data.speed = v),
		new FloatConfigValue<>("Direction", Range.of(0F, 360F), true, data -> data.direction, (data, v) -> data.direction = v),
		new FloatConfigValue<>("Tilt", Range.of(0F, 180F), true, data -> data.tilt, (data, v) -> data.tilt = v),
		new RangeConfigValue<>("Section", Range.of(-360F, 360F), false, data -> data.section, (data, v) -> data.section = v),
		new BooleanConfigValue<>("Ignore Block Density", data -> data.ignoreBlockDensity, (data, v) -> data.ignoreBlockDensity = v)
	);

	public static final VLRegistry<PhysicsParticleData> REGISTRY = VLRegistry.createClient("physics_particle_data", PhysicsParticleData.class);

	public static class Loader extends JsonCodecReloadListener<PhysicsParticleData> {
		public Loader() {
			super("vidlib/physics_particle_data", CODEC, false);
		}

		@Override
		protected void apply(Map<ResourceLocation, PhysicsParticleData> map) {
			REGISTRY.update(Map.copyOf(map));
		}
	}

	public float density = DEFAULT_DENSITY;
	public Range lifespan = DEFAULT_LIFESPAN;
	public Range scale = DEFAULT_SCALE;
	public Range power = DEFAULT_POWER;
	public Range spread = DEFAULT_SPREAD;
	public float inertia = DEFAULT_INERTIA;
	public float gravity = DEFAULT_GRAVITY;
	public Range speed = DEFAULT_SPEED;
	public float direction = DEFAULT_DIRECTION;
	public float tilt = DEFAULT_TILT;
	public Range section = DEFAULT_SECTION;
	public boolean ignoreBlockDensity = DEFAULT_IGNORE_BLOCK_DENSITY;

	public PhysicsParticleData() {
	}

	private PhysicsParticleData(
		float density,
		Range lifespan,
		Range scale,
		Range power,
		Range spread,
		float inertia,
		float gravity,
		Range speed,
		float direction,
		float tilt,
		Range section,
		boolean ignoreBlockDensity
	) {
		this.density = density;
		this.lifespan = lifespan;
		this.scale = scale;
		this.power = power;
		this.spread = spread;
		this.inertia = inertia;
		this.gravity = gravity;
		this.speed = speed;
		this.direction = direction;
		this.tilt = tilt;
		this.section = section;
		this.ignoreBlockDensity = ignoreBlockDensity;
	}

	@Override
	public String toString() {
		return "PhysicsParticleData[" +
			"density=" + density +
			", lifespan=" + lifespan +
			", scale=" + scale +
			", power=" + power +
			", spread=" + spread +
			", inertia=" + inertia +
			", gravity=" + gravity +
			", speed=" + speed +
			", direction=" + direction +
			", tilt=" + tilt +
			", section=" + section +
			", ignoreBlockDensity=" + ignoreBlockDensity +
			']';
	}
}
