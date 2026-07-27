package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Range;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.UnitType;
import dev.latvian.mods.klib.util.JsonRegistryReloadListener;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PhysicsParticleData(
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
	boolean ignoreBlockDensity,
	float renderDistance
) implements CustomRegistryValue<ByteBuf, PhysicsParticleData> {
	public static final PhysicsParticleData DEFAULT_VALUE = new PhysicsParticleData(
		4F,
		Range.of(80F, 120F),
		Range.of(1F, 2F),
		Range.of(0.2F, 2F),
		Range.of(0F, 0.3F),
		0.96F,
		0.036F,
		Range.ONE,
		0F,
		0F,
		Range.of(0F, 360F),
		false,
		8192F
	);

	public static final UnitType<ByteBuf, PhysicsParticleData> DEFAULT = UnitType.create("default", DEFAULT_VALUE);

	public static class Builder {
		public float density = DEFAULT_VALUE.density;
		public Range lifespan = DEFAULT_VALUE.lifespan;
		public Range scale = DEFAULT_VALUE.scale;
		public Range power = DEFAULT_VALUE.power;
		public Range spread = DEFAULT_VALUE.spread;
		public float inertia = DEFAULT_VALUE.inertia;
		public float gravity = DEFAULT_VALUE.gravity;
		public Range speed = DEFAULT_VALUE.speed;
		public float direction = DEFAULT_VALUE.direction;
		public float tilt = DEFAULT_VALUE.tilt;
		public Range section = DEFAULT_VALUE.section;
		public boolean ignoreBlockDensity = DEFAULT_VALUE.ignoreBlockDensity;
		public float renderDistance = DEFAULT_VALUE.renderDistance;

		public Builder density(float value) {
			this.density = value;
			return this;
		}

		public Builder lifespan(Range value) {
			this.lifespan = value;
			return this;
		}

		public Builder scale(Range value) {
			this.scale = value;
			return this;
		}

		public Builder power(Range value) {
			this.power = value;
			return this;
		}

		public Builder spread(Range value) {
			this.spread = value;
			return this;
		}

		public Builder inertia(float value) {
			this.inertia = value;
			return this;
		}

		public Builder gravity(float value) {
			this.gravity = value;
			return this;
		}

		public Builder speed(Range value) {
			this.speed = value;
			return this;
		}

		public Builder direction(float value) {
			this.direction = value;
			return this;
		}

		public Builder tilt(float value) {
			this.tilt = value;
			return this;
		}

		public Builder section(Range value) {
			this.section = value;
			return this;
		}

		public Builder ignoreBlockDensity(boolean value) {
			this.ignoreBlockDensity = value;
			return this;
		}

		public Builder renderDistance(float value) {
			this.renderDistance = value;
			return this;
		}

		public PhysicsParticleData build() {
			return new PhysicsParticleData(
				density,
				lifespan,
				scale,
				power,
				spread,
				inertia,
				gravity,
				speed,
				direction,
				tilt,
				section,
				ignoreBlockDensity,
				renderDistance
			);
		}
	}

	public static final MapCodec<PhysicsParticleData> DIRECT_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.FLOAT.optionalFieldOf("density", DEFAULT_VALUE.density).forGetter(PhysicsParticleData::density),
		Range.CODEC.optionalFieldOf("lifespan", DEFAULT_VALUE.lifespan).forGetter(PhysicsParticleData::lifespan),
		Range.CODEC.optionalFieldOf("scale", DEFAULT_VALUE.scale).forGetter(PhysicsParticleData::scale),
		Range.CODEC.optionalFieldOf("power", DEFAULT_VALUE.power).forGetter(PhysicsParticleData::power),
		Range.CODEC.optionalFieldOf("spread", DEFAULT_VALUE.spread).forGetter(PhysicsParticleData::spread),
		Codec.FLOAT.optionalFieldOf("inertia", DEFAULT_VALUE.inertia).forGetter(PhysicsParticleData::inertia),
		Codec.FLOAT.optionalFieldOf("gravity", DEFAULT_VALUE.gravity).forGetter(PhysicsParticleData::gravity),
		Range.CODEC.optionalFieldOf("speed", DEFAULT_VALUE.speed).forGetter(PhysicsParticleData::speed),
		Codec.FLOAT.optionalFieldOf("direction", DEFAULT_VALUE.direction).forGetter(PhysicsParticleData::direction),
		Codec.FLOAT.optionalFieldOf("tilt", DEFAULT_VALUE.tilt).forGetter(PhysicsParticleData::tilt),
		Range.CODEC.optionalFieldOf("section", DEFAULT_VALUE.section).forGetter(PhysicsParticleData::section),
		Codec.BOOL.optionalFieldOf("ignore_block_density", DEFAULT_VALUE.ignoreBlockDensity).forGetter(PhysicsParticleData::ignoreBlockDensity),
		Codec.FLOAT.optionalFieldOf("render_distance", DEFAULT_VALUE.renderDistance).forGetter(PhysicsParticleData::renderDistance)
	).apply(instance, PhysicsParticleData::new));

	public static final StreamCodec<ByteBuf, PhysicsParticleData> DIRECT_STREAM_CODEC = ByteBufCodecs.fromCodecTrusted(RecordCodecBuilder.create(instance -> instance.group(
		Codec.FLOAT.optionalFieldOf("d", DEFAULT_VALUE.density).forGetter(PhysicsParticleData::density),
		Range.CODEC.optionalFieldOf("l", DEFAULT_VALUE.lifespan).forGetter(PhysicsParticleData::lifespan),
		Range.CODEC.optionalFieldOf("s", DEFAULT_VALUE.scale).forGetter(PhysicsParticleData::scale),
		Range.CODEC.optionalFieldOf("p", DEFAULT_VALUE.power).forGetter(PhysicsParticleData::power),
		Range.CODEC.optionalFieldOf("r", DEFAULT_VALUE.spread).forGetter(PhysicsParticleData::spread),
		Codec.FLOAT.optionalFieldOf("i", DEFAULT_VALUE.inertia).forGetter(PhysicsParticleData::inertia),
		Codec.FLOAT.optionalFieldOf("g", DEFAULT_VALUE.gravity).forGetter(PhysicsParticleData::gravity),
		Range.CODEC.optionalFieldOf("f", DEFAULT_VALUE.speed).forGetter(PhysicsParticleData::speed),
		Codec.FLOAT.optionalFieldOf("d", DEFAULT_VALUE.direction).forGetter(PhysicsParticleData::direction),
		Codec.FLOAT.optionalFieldOf("t", DEFAULT_VALUE.tilt).forGetter(PhysicsParticleData::tilt),
		Range.CODEC.optionalFieldOf("c", DEFAULT_VALUE.section).forGetter(PhysicsParticleData::section),
		Codec.BOOL.optionalFieldOf("bd", DEFAULT_VALUE.ignoreBlockDensity).forGetter(PhysicsParticleData::ignoreBlockDensity),
		Codec.FLOAT.optionalFieldOf("rd", DEFAULT_VALUE.renderDistance).forGetter(PhysicsParticleData::renderDistance)
	).apply(instance, PhysicsParticleData::new)));

	public static final DynamicType<ByteBuf, PhysicsParticleData> TYPE = DynamicType.create(
		"default",
		DIRECT_MAP_CODEC,
		DIRECT_STREAM_CODEC
	);

	public static final CustomRegistry<ByteBuf, PhysicsParticleData> REGISTRY = CustomRegistry.createNoValueSync("physics_particle_data", TYPE);
	public static final Codec<Ref<PhysicsParticleData>> CODEC = REGISTRY.codec();
	public static final StreamCodec<ByteBuf, Ref<PhysicsParticleData>> STREAM_CODEC = REGISTRY.streamCodec();
	public static final DataType<Ref<PhysicsParticleData>> DATA_TYPE = REGISTRY.dataType();

	public static class ClientLoader extends JsonRegistryReloadListener<PhysicsParticleData> {
		public ClientLoader() {
			super("vidlib/physics_particle_data", REGISTRY);
		}
	}

	@Override
	public CustomRegistry<ByteBuf, PhysicsParticleData> getRegistry() {
		return REGISTRY;
	}

	@Override
	public CustomRegistryType<ByteBuf, PhysicsParticleData> type() {
		return TYPE;
	}
}
