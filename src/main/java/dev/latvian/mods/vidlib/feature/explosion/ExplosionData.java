package dev.latvian.mods.vidlib.feature.explosion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.block.BlockUtils;
import dev.latvian.mods.klib.block.filter.BlockFilter;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.entity.filter.EntityFilter;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.EasingType;
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
import java.util.Objects;

public class ExplosionData {
	public static final ExplosionData DEFAULT = new ExplosionData();

	public static class EntityData {
		public static final EntityData DEFAULT = new EntityData();

		public static final Codec<EntityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("min_damage", 0F).forGetter(v -> v.minDamage),
			Codec.FLOAT.optionalFieldOf("max_damage", 4F).forGetter(v -> v.maxDamage),
			EasingType.CODEC.optionalFieldOf("damage_easing", EasingType.IN_CUBIC).forGetter(v -> v.damageEase),
			Codec.FLOAT.optionalFieldOf("horizontal_knockback", 1F).forGetter(v -> v.horizontalKnockback),
			Codec.FLOAT.optionalFieldOf("vertical_knockback", 0F).forGetter(v -> v.verticalKnockback),
			Codec.BOOL.optionalFieldOf("spherical", false).forGetter(v -> v.spherical),
			Codec.FLOAT.optionalFieldOf("radius_mod", 1.05F).forGetter(v -> v.radiusMod)
		).apply(instance, EntityData::new));

		public static final StreamCodec<ByteBuf, EntityData> STREAM_CODEC = CompositeStreamCodec.of(
			KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 0F), v -> v.minDamage,
			ByteBufCodecs.FLOAT, v -> v.maxDamage,
			MCStreamCodecs.EASING_TYPE, v -> v.damageEase,
			KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 1F), v -> v.horizontalKnockback,
			KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 0F), v -> v.verticalKnockback,
			ByteBufCodecs.BOOL, v -> v.spherical,
			KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 1.05F), v -> v.radiusMod,
			EntityData::new
		);

		public float minDamage;
		public float maxDamage;
		public EasingType damageEase;
		public float horizontalKnockback;
		public float verticalKnockback;
		public boolean spherical;
		public float radiusMod;

		public EntityData() {
			this.minDamage = 0F;
			this.maxDamage = 4F;
			this.damageEase = EasingType.IN_CUBIC;
			this.horizontalKnockback = 1F;
			this.verticalKnockback = 0F;
			this.spherical = false;
			this.radiusMod = 1.05F;
		}

		private EntityData(
			float minDamage,
			float maxDamage,
			EasingType damageEase,
			float horizontalKnockback,
			float verticalKnockback,
			boolean spherical,
			float radiusMod
		) {
			this.minDamage = minDamage;
			this.maxDamage = maxDamage;
			this.damageEase = damageEase;
			this.horizontalKnockback = horizontalKnockback;
			this.verticalKnockback = verticalKnockback;
			this.spherical = spherical;
			this.radiusMod = radiusMod;
		}

		private EntityData copy() {
			return new EntityData(minDamage, maxDamage, damageEase, horizontalKnockback, verticalKnockback, spherical, radiusMod);
		}

		@Override
		public String toString() {
			return "EntityData[" +
				"minDamage=" + minDamage +
				", maxDamage=" + maxDamage +
				", damageEasing=" + damageEase +
				", horizontalKnockback=" + horizontalKnockback +
				", verticalKnockback=" + verticalKnockback +
				']';
		}

		@Override
		public int hashCode() {
			return Objects.hash(
				minDamage,
				maxDamage,
				damageEase,
				horizontalKnockback,
				verticalKnockback,
				spherical,
				radiusMod
			);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof EntityData d) {
				return minDamage == d.minDamage
					&& maxDamage == d.maxDamage
					&& damageEase == d.damageEase
					&& horizontalKnockback == d.horizontalKnockback
					&& verticalKnockback == d.verticalKnockback
					&& spherical == d.spherical
					&& radiusMod == d.radiusMod;
			} else {
				return false;
			}
		}

		public float damage(float relativeDistance) {
			return KMath.lerp(Math.clamp(damageEase.apply(relativeDistance), 0F, 1F), maxDamage, minDamage);
		}
	}

	public static class FilterData {
		public static final FilterData DEFAULT = new FilterData();

		public static final Codec<FilterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("floor", -1000).forGetter(v -> v.floor),
			Codec.INT.optionalFieldOf("ceiling", 1000).forGetter(v -> v.ceiling),
			BlockFilter.CODEC.optionalFieldOf("blocks", BlockFilter.ANY).forGetter(v -> v.blocks),
			EntityFilter.CODEC.optionalFieldOf("ignored", EntityFilter.CREATIVE_MODE).forGetter(v -> v.ignored),
			EntityFilter.CODEC.optionalFieldOf("invincible", EntityFilter.NONE).forGetter(v -> v.invincible),
			Codec.BOOL.optionalFieldOf("bypass_unbreakable", false).forGetter(v -> v.bypassUnbreakable)
		).apply(instance, FilterData::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, FilterData> STREAM_CODEC = CompositeStreamCodec.of(
			KLibStreamCodecs.optional(ByteBufCodecs.VAR_INT, -1000), v -> v.floor,
			KLibStreamCodecs.optional(ByteBufCodecs.VAR_INT, 1000), v -> v.ceiling,
			KLibStreamCodecs.optional(BlockFilter.STREAM_CODEC, BlockFilter.ANY), v -> v.blocks,
			KLibStreamCodecs.optional(EntityFilter.STREAM_CODEC, EntityFilter.CREATIVE_MODE), v -> v.ignored,
			KLibStreamCodecs.optional(EntityFilter.STREAM_CODEC, EntityFilter.NONE), v -> v.invincible,
			ByteBufCodecs.BOOL, v -> v.bypassUnbreakable,
			FilterData::new
		);

		public int floor;
		public int ceiling;
		public Ref<BlockFilter> blocks;
		public Ref<EntityFilter> ignored;
		public Ref<EntityFilter> invincible;
		public boolean bypassUnbreakable;

		public FilterData() {
			this.floor = -1000;
			this.ceiling = 1000;
			this.blocks = BlockFilter.ANY;
			this.ignored = EntityFilter.CREATIVE_MODE;
			this.invincible = EntityFilter.NONE;
			this.bypassUnbreakable = false;
		}

		private FilterData(
			int floor,
			int ceiling,
			Ref<BlockFilter> blocks,
			Ref<EntityFilter> ignored,
			Ref<EntityFilter> invincible,
			boolean bypassUnbreakable
		) {
			this.floor = -1000;
			this.ceiling = 1000;
			this.blocks = BlockFilter.ANY;
			this.ignored = EntityFilter.CREATIVE_MODE;
			this.invincible = EntityFilter.NONE;
			this.bypassUnbreakable = false;
		}

		private FilterData copy() {
			return new FilterData(floor, ceiling, blocks, ignored, invincible, bypassUnbreakable);
		}

		@Override
		public String toString() {
			return "FilterData[" +
				"floor=" + floor +
				", ceiling=" + ceiling +
				", blocks=" + blocks +
				", ignored=" + ignored +
				", invincible=" + invincible +
				", bypassUnbreakable=" + bypassUnbreakable +
				']';
		}

		@Override
		public int hashCode() {
			return Objects.hash(
				floor,
				ceiling,
				blocks,
				ignored,
				invincible,
				bypassUnbreakable
			);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof FilterData d) {
				return floor == d.floor
					&& ceiling == d.ceiling
					&& blocks.equals(d.blocks)
					&& ignored.equals(d.ignored)
					&& invincible.equals(d.invincible)
					&& bypassUnbreakable == d.bypassUnbreakable;
			} else {
				return false;
			}
		}
	}

	public static final Codec<ExplosionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.FLOAT.optionalFieldOf("radius", 4F).forGetter(v -> v.radius),
		Codec.FLOAT.optionalFieldOf("depth", 4F).forGetter(v -> v.height),
		Codec.FLOAT.optionalFieldOf("height", 4F).forGetter(v -> v.depth),
		Codec.FLOAT.optionalFieldOf("destroy", 1F).forGetter(v -> v.destroy),
		Codec.FLOAT.optionalFieldOf("decay", 0.3F).forGetter(v -> v.decay),
		Codec.FLOAT.optionalFieldOf("fire", 0F).forGetter(v -> v.fire),
		Codec.BOOL.optionalFieldOf("smolder", false).forGetter(v -> v.smolder),
		EntityData.CODEC.optionalFieldOf("entity", EntityData.DEFAULT).forGetter(v -> v.entity),
		FilterData.CODEC.optionalFieldOf("filter", FilterData.DEFAULT).forGetter(v -> v.filter)
	).apply(instance, ExplosionData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ExplosionData> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT, v -> v.radius,
		ByteBufCodecs.FLOAT, v -> v.depth,
		ByteBufCodecs.FLOAT, v -> v.height,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 1F), v -> v.destroy,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 0.3F), v -> v.decay,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 0F), v -> v.fire,
		ByteBufCodecs.BOOL, v -> v.smolder,
		KLibStreamCodecs.optional(EntityData.STREAM_CODEC, EntityData.DEFAULT), v -> v.entity,
		KLibStreamCodecs.optional(FilterData.STREAM_CODEC, FilterData.DEFAULT), v -> v.filter,
		ExplosionData::new
	);

	public static final DataType<ExplosionData> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC);

	public float radius;
	public float depth;
	public float height;
	public float destroy;
	public float decay;
	public float fire;
	public boolean smolder;
	public EntityData entity;
	public FilterData filter;

	public ExplosionData() {
		this.radius = 4F;
		this.depth = 4F;
		this.height = 4F;
		this.destroy = 1F;
		this.decay = 0.3F;
		this.fire = 0F;
		this.smolder = false;
		this.entity = new EntityData();
		this.filter = new FilterData();
	}

	private ExplosionData(
		float radius,
		float depth,
		float height,
		float destroy,
		float decay,
		float fire,
		boolean smolder,
		EntityData entity,
		FilterData filter
	) {
		this.radius = radius;
		this.depth = depth;
		this.height = height;
		this.destroy = destroy;
		this.decay = decay;
		this.fire = fire;
		this.smolder = smolder;
		this.entity = entity.copy();
		this.filter = filter.copy();
	}

	public void setSize(float size) {
		this.radius = size;
		this.depth = size;
		this.height = size;
	}

	public void debugText(ScreenText.ScreenTextList list) {
	}

	public ExplosionInstance instance(Level level, BlockPos at) {
		return new ExplosionInstance(level, at, this);
	}

	public AABB getBounds(Vec3 at) {
		return new AABB(at.x - radius, at.y - depth, at.z - radius, at.x + radius, at.y + height, at.z + radius);
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
		int starty = Math.max(aty - ivradiusd, filter.floor);
		int endy = Math.min(aty + ivradiusu, filter.ceiling);

		for (int ay = starty; ay <= endy; ay++) {
			int y = ay - aty;
			pos.setY(ay);

			for (int x = -ihradius; x <= ihradius; x++) {
				for (int z = -ihradius; z <= ihradius; z++) {
					// https://en.wikipedia.org/wiki/Ellipsoid

					var inside = inside(x, y, z);

					if (inside <= 1F) {
						pos.setX(atx + x);
						pos.setZ(atz + z);
						var state = level.getBlockState(pos);

						if (BlockUtils.getDensity(state) > 0F && (filter.bypassUnbreakable || state.getDestroySpeed(level, pos) >= 0F) || state.getBlock() instanceof BaseFireBlock) {
							if (filter.blocks.value().test(level, pos, state)) {
								blocks.add(new DestroyedBlock(pos.immutable(), state, x, y, z, inside, new MutableBoolean(false)));
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
		return entity.isAlive() && !entity.isSpectator() && !filter.ignored.value().test(entity);
	}

	public double entityRangeInflation() {
		return 0.5D; // Math.max(radius, Math.max(depth, height)) * 2D;
	}

	public List<Entity> collectEntities(Level level, Vec3 at) {
		var entities = new ArrayList<Entity>();

		double atx = at.x;
		double aty = at.y;
		double atz = at.z;

		double exp = entityRangeInflation();

		for (var e : level.getEntities((Entity) null, new AABB(atx - radius - exp, aty - depth - exp, atz - radius - exp, atx + radius + exp, aty + height + exp, atz + radius + exp), this::includeEntity)) {
			float x = (float) (e.getX() - atx);
			float y = entity.spherical ? (float) (e.getY() - aty) : 0F;
			float z = (float) (e.getZ() - atz);
			var d = KMath.sq(x / radius) + (y == 0F ? 0F : y > 0 ? KMath.sq(y / height) : KMath.sq(y / depth)) + KMath.sq(z / radius);

			if (d <= entity.radiusMod) {
				entities.add(e);
			}
		}

		return entities;
	}

	public void damageEntities(ServerLevel level, Vec3 at, List<Entity> entities) {
		if (entity.minDamage <= 0F && entity.maxDamage <= 0F) {
			return;
		}

		var source = level.damageSources().explosion(null, null);
		double atx = at.x;
		double aty = at.y;
		double atz = at.z;

		for (var e : entities) {
			double x = e.getX() - atx;
			double y = entity.spherical ? ((e instanceof PrimedTnt ? e.getY() : e.getEyeY()) - aty) : 0D;
			double z = e.getZ() - atz;

			double inside = Math.min(inside((float) x, (float) y, (float) z), entity.radiusMod);

			if (inside <= entity.radiusMod) {
				var damage = entity.damage((float) inside / entity.radiusMod);

				if (e instanceof LivingEntity l && filter.invincible.value().test(l)) {
					var h = l.getHealth();
					var hp = Math.max(1F, h - damage);
					float f = h - hp;
					e.hurtServer(level, source, f <= 0F ? 0.001F : f);
				} else {
					e.hurtServer(level, source, damage);
				}
			}
		}
	}

	public void knockBackEntities(Vec3 at, List<Entity> entities) {
		if (entity.horizontalKnockback <= 0F && entity.verticalKnockback <= 0F) {
			return;
		}

		double atx = at.x;
		double aty = at.y;
		double atz = at.z;

		for (var e : entities) {
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
				double ad;

				if (e instanceof LivingEntity livingEntity) {
					ad = ac * (1D - livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE));
				} else {
					ad = ac;
				}

				x *= ad * entity.horizontalKnockback;
				y *= ad * entity.horizontalKnockback;
				z *= ad * entity.horizontalKnockback;
				y += entity.verticalKnockback;

				e.forceAddVelocity(new Vec3(x, y, z));
			}
		}
	}

	public void igniteEntities(List<Entity> entities) {
		if (fire <= 0F) {
			return;
		}

		for (var e : entities) {
			if (!e.fireImmune()) {
				e.igniteForTicks((int) (fire * 10));
			}
		}
	}

	@Override
	public String toString() {
		return "ExplosionData[" +
			"radius=" + radius +
			", depth=" + depth +
			", height=" + height +
			", destroy=" + destroy +
			", decay=" + decay +
			", fire=" + fire +
			", smolder=" + smolder +
			", entity=" + entity +
			", filter=" + filter +
			']';
	}
}
