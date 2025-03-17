package dev.beast.mods.shimmer.feature.explosion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.config.BooleanConfigValue;
import dev.beast.mods.shimmer.feature.config.ConfigValue;
import dev.beast.mods.shimmer.feature.config.FloatConfigValue;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.misc.DebugText;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.math.Range;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.List;

public class ExplosionData {
	public static final Codec<ExplosionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.FLOAT.optionalFieldOf("radius", 4F).forGetter(v -> v.radius),
		Codec.FLOAT.optionalFieldOf("depth", 4F).forGetter(v -> v.height),
		Codec.FLOAT.optionalFieldOf("height", 4F).forGetter(v -> v.depth),
		Codec.FLOAT.optionalFieldOf("destroy", 1F).forGetter(v -> v.destroy),
		Codec.FLOAT.optionalFieldOf("decay", 1F).forGetter(v -> v.decay),
		Codec.FLOAT.optionalFieldOf("fire", 0F).forGetter(v -> v.fire),
		Codec.BOOL.optionalFieldOf("smolder", false).forGetter(v -> v.smolder),
		Codec.FLOAT.optionalFieldOf("entity_damage", 4F).forGetter(v -> v.entityDamage),
		Codec.FLOAT.optionalFieldOf("entity_knockback", 1F).forGetter(v -> v.entityKnockback),
		Codec.INT.optionalFieldOf("floor", -1000).forGetter(v -> v.floor),
		Codec.INT.optionalFieldOf("ceiling", 1000).forGetter(v -> v.ceiling),
		BlockFilter.CODEC.optionalFieldOf("block_filter", BlockFilter.ANY.instance()).forGetter(v -> v.blockFilter),
		EntityFilter.CODEC.optionalFieldOf("ignored_entities", EntityFilter.CREATIVE.instance()).forGetter(v -> v.ignoredEntities),
		EntityFilter.CODEC.optionalFieldOf("invincible_entities", EntityFilter.NONE.instance()).forGetter(v -> v.invincibleEntities)
	).apply(instance, ExplosionData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ExplosionData> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT, v -> v.radius,
		ByteBufCodecs.FLOAT, v -> v.depth,
		ByteBufCodecs.FLOAT, v -> v.height,
		ByteBufCodecs.FLOAT, v -> v.destroy,
		ByteBufCodecs.FLOAT, v -> v.decay,
		ByteBufCodecs.FLOAT, v -> v.fire,
		ByteBufCodecs.BOOL, v -> v.smolder,
		ByteBufCodecs.FLOAT, v -> v.entityDamage,
		ByteBufCodecs.FLOAT, v -> v.entityKnockback,
		ByteBufCodecs.INT, v -> v.floor,
		ByteBufCodecs.INT, v -> v.ceiling,
		BlockFilter.STREAM_CODEC.optional(BlockFilter.ANY.instance()), v -> v.blockFilter,
		EntityFilter.STREAM_CODEC.optional(EntityFilter.CREATIVE.instance()), v -> v.ignoredEntities,
		EntityFilter.STREAM_CODEC.optional(EntityFilter.NONE.instance()), v -> v.invincibleEntities,
		ExplosionData::new
	);

	public static final List<ConfigValue<ExplosionData, ?>> CONFIG = List.of(
		new FloatConfigValue<>("Radius", Range.of(0F, 500F), false, data -> data.radius, (data, v) -> data.radius = v),
		new FloatConfigValue<>("Depth", Range.of(0F, 500F), false, data -> data.depth, (data, v) -> data.depth = v),
		new FloatConfigValue<>("Height", Range.of(0F, 500F), false, data -> data.height, (data, v) -> data.height = v),
		new FloatConfigValue<>("Destroy", Range.FULL, true, data -> data.destroy, (data, v) -> data.destroy = v),
		new FloatConfigValue<>("Decay", Range.FULL, true, data -> data.decay, (data, v) -> data.decay = v),
		new FloatConfigValue<>("Fire", Range.FULL, true, data -> data.fire, (data, v) -> data.fire = v),
		new BooleanConfigValue<>("Smolder", data -> data.smolder, (data, v) -> data.smolder = v),
		new FloatConfigValue<>("Entity Damage", Range.of(0F, 100F), false, data -> data.entityDamage, (data, v) -> data.entityDamage = v),
		new FloatConfigValue<>("Entity Knockback", Range.of(0F, 100F), false, data -> data.entityKnockback, (data, v) -> data.entityKnockback = v),
		new ConfigValue<>("Floor", Codec.INT, data -> data.floor, (data, v) -> data.floor = v),
		new ConfigValue<>("Ceiling", Codec.INT, data -> data.ceiling, (data, v) -> data.ceiling = v),
		new ConfigValue<>("Block Filter", BlockFilter.CODEC, data -> data.blockFilter, (data, v) -> data.blockFilter = v),
		new ConfigValue<>("Ignored Entities", EntityFilter.CODEC, data -> data.ignoredEntities, (data, v) -> data.ignoredEntities = v),
		new ConfigValue<>("Invincible Entities", EntityFilter.CODEC, data -> data.invincibleEntities, (data, v) -> data.invincibleEntities = v)
	);

	public float radius;
	public float depth;
	public float height;
	public float destroy;
	public float decay;
	public float fire;
	public boolean smolder;
	public float entityDamage;
	public float entityKnockback;
	public int floor;
	public int ceiling;
	public BlockFilter blockFilter;
	public EntityFilter ignoredEntities;
	public EntityFilter invincibleEntities;

	public ExplosionData() {
		this.radius = 4F;
		this.depth = 4F;
		this.height = 4F;
		this.destroy = 1F;
		this.decay = 1F;
		this.fire = 0F;
		this.smolder = false;
		this.entityDamage = 4F;
		this.entityKnockback = 1F;
		this.floor = -1000;
		this.ceiling = 1000;
		this.blockFilter = BlockFilter.ANY.instance();
		this.ignoredEntities = EntityFilter.CREATIVE.instance();
		this.invincibleEntities = EntityFilter.NONE.instance();
	}

	private ExplosionData(
		float radius,
		float depth,
		float height,
		float destroy,
		float decay,
		float fire,
		boolean smolder,
		float entityDamage,
		float entityKnockback,
		int floor,
		int ceiling,
		BlockFilter blockFilter,
		EntityFilter ignoredEntities,
		EntityFilter invincibleEntities
	) {
		this.radius = radius;
		this.depth = depth;
		this.height = height;
		this.destroy = destroy;
		this.decay = decay;
		this.fire = fire;
		this.smolder = smolder;
		this.entityDamage = entityDamage;
		this.entityKnockback = entityKnockback;
		this.floor = floor;
		this.ceiling = ceiling;
		this.blockFilter = blockFilter;
		this.ignoredEntities = ignoredEntities;
		this.invincibleEntities = invincibleEntities;
	}

	public void setSize(float size) {
		this.radius = size;
		this.depth = size;
		this.height = size;
	}

	public void debugText(DebugText.DebugTextList list) {
		list.addConfig(this, CONFIG);
	}

	public ExplosionInstance instance(Level level, BlockPos at) {
		return new ExplosionInstance(level, at, this);
	}

	public float inside(float rx, float ry, float rz) {
		return KMath.sq(rx / radius) + (Math.abs(ry) <= 0.001F ? 0F : ry > 0F ? KMath.sq(ry / height) : KMath.sq(ry / depth)) + KMath.sq(rz / radius);
	}

	public List<DestroyedBlock> collectBlocks(Level level, BlockPos at) {
		var blocks = new ArrayList<DestroyedBlock>();

		int atx = at.getX();
		int aty = at.getY();
		int atz = at.getZ();

		var pos = new BlockPos.MutableBlockPos();

		int ihradius = KMath.ceil(radius);
		int ivradiusd = KMath.ceil(depth);
		int ivradiusu = KMath.ceil(height);
		int starty = Math.max(aty - ivradiusd, floor);
		int endy = Math.min(aty + ivradiusu, ceiling);

		for (int ay = starty; ay <= endy; ay++) {
			int y = ay - aty;
			pos.setY(ay);

			for (int x = -ihradius; x <= ihradius; x++) {
				for (int z = -ihradius; z <= ihradius; z++) {
					// https://en.wikipedia.org/wiki/Ellipsoid

					var d = inside(x, y, z);

					if (d <= 1F) {
						pos.setX(atx + x);
						pos.setZ(atz + z);
						var state = level.getBlockState(pos);

						if (state.shimmer$getDensity() > 0F && state.getDestroySpeed(level, pos) >= 0F || state.getBlock() instanceof BaseFireBlock) {
							if (blockFilter.test(level, pos, state)) {
								blocks.add(new DestroyedBlock(pos.immutable(), state, x, y, z, d, new MutableBoolean(false)));
							}
						}
					}
				}
			}
		}

		blocks.sort(null);
		return blocks;
	}

	public boolean includeEntity(Entity entity) {
		return entity.isAlive() && !entity.isSpectator() && !ignoredEntities.test(entity);
	}

	public List<Entity> collectEntities(Level level, Vec3 at) {
		var entities = new ArrayList<Entity>();

		double atx = at.x;
		double aty = at.y;
		double atz = at.z;

		double exp = 0.5D;

		for (var entity : level.getEntities((Entity) null, new AABB(atx - radius - exp, aty - depth - exp, atz - radius - exp, atx + radius + exp, aty + height + exp, atz + radius + exp), this::includeEntity)) {
			double x = entity.getX() - atx;
			double y = entity.getY() - aty;
			double z = entity.getZ() - atz;
			var d = KMath.sq(x / radius) + (y == 0 ? 0 : y > 0 ? KMath.sq(y / height) : KMath.sq(y / depth)) + KMath.sq(z / radius);

			if (d <= 1.05F) {
				entities.add(entity);
			}
		}

		return entities;
	}

	public void damageEntities(Level level, Vec3 at, List<Entity> entities) {
		var source = level.damageSources().explosion(null, null);
		double atx = at.x;
		double aty = at.y;
		double atz = at.z;

		for (var e : entities) {
			if (entityDamage > 0F) {
				double x = e.getX() - atx;
				double y = (e instanceof PrimedTnt ? e.getY() : e.getEyeY()) - aty;
				double z = e.getZ() - atz;

				double q = Math.min(inside((float) x, (float) y, (float) z), 1D);
				double aa = Math.sqrt(x * x + y * y + z * z);

				if (aa > 0D) {
					x /= aa;
					y /= aa;
					z /= aa;
					double ac = 1D - q;

					var ed = KMath.lerp(q, entityDamage, entityDamage / 3D);

					if (e instanceof LivingEntity l && invincibleEntities.test(l)) {
						var h = l.getHealth();
						var hp = Math.max(1D, h - ed);
						float f = (float) (h - hp);
						e.hurt(source, f <= 0F ? 0.001F : f);
					} else {
						e.hurt(source, (float) ed);
					}

					double ad;

					if (e instanceof LivingEntity livingEntity) {
						ad = ac * (1D - livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE));
					} else {
						ad = ac;
					}

					x *= ad * entityKnockback;
					y *= ad * entityKnockback;
					z *= ad * entityKnockback;
					y += 0.1D * entityDamage;

					e.setDeltaMovement(e.getDeltaMovement().add(new Vec3(x, y, z)));
				}
			}

			if (fire > 0F && !e.fireImmune()) {
				e.igniteForTicks((int) (fire * 10));
			}
		}
	}
}
