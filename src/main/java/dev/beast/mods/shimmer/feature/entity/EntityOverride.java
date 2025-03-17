package dev.beast.mods.shimmer.feature.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.core.ShimmerEntity;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.icon.IconHolder;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EntityOverride<T> {
	private static final Map<String, EntityOverride<?>> MAP = new HashMap<>();

	public static final Codec<EntityOverride<?>> CODEC = ShimmerCodecs.map(() -> MAP, Codec.STRING, EntityOverride::id);
	public static final Codec<Map<EntityOverride<?>, Object>> OVERRIDE_MAP_CODEC = Codec.dispatchedMap(CODEC, EntityOverride::codec);

	public static final StreamCodec<RegistryFriendlyByteBuf, Map<EntityOverride<?>, Object>> OVERRIDE_MAP_STREAM_CODEC = new StreamCodec<>() {
		@Override
		public Map<EntityOverride<?>, Object> decode(RegistryFriendlyByteBuf buf) {
			int count = buf.readVarInt();

			if (count == 0) {
				return Map.of();
			}

			var map = new HashMap<EntityOverride<?>, Object>(count);

			for (int i = 0; i < count; i++) {
				var key = MAP.get(buf.readUtf());
				var value = key.streamCodec().decode(buf);
				map.put(key, value);
			}

			return map;
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, Map<EntityOverride<?>, Object> map) {
			buf.writeVarInt(map.size());

			for (var entry : map.entrySet()) {
				buf.writeUtf(entry.getKey().id());
				entry.getKey().streamCodec().encode(buf, Cast.to(entry.getValue()));
			}
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> EntityOverride<T> createKey(String id, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return (EntityOverride<T>) MAP.computeIfAbsent(id, k -> new EntityOverride<>(k, codec, streamCodec));
	}

	public static <T> EntityOverride<T> createKey(String id, Codec<T> codec) {
		return createKey(id, codec, ByteBufCodecs.fromCodecWithRegistries(codec));
	}

	public static EntityOverride<Boolean> createBooleanKey(String id) {
		return createKey(id, Codec.BOOL, ByteBufCodecs.BOOL);
	}

	public static EntityOverride<Integer> createIntKey(String id) {
		return createKey(id, Codec.INT, ByteBufCodecs.INT);
	}

	public static EntityOverride<Color> createColorKey(String id) {
		return createKey(id, Color.CODEC, Color.STREAM_CODEC);
	}

	public static EntityOverride<Integer> createVarIntKey(String id) {
		return createKey(id, Codec.INT, ByteBufCodecs.VAR_INT);
	}

	public static EntityOverride<Float> createFloatKey(String id) {
		return createKey(id, Codec.FLOAT, ByteBufCodecs.FLOAT);
	}

	public static EntityOverride<Double> createDoubleKey(String id) {
		return createKey(id, Codec.DOUBLE, ByteBufCodecs.DOUBLE);
	}

	public static EntityOverride<ItemStack> createItemKey(String id) {
		return createKey(id, ItemStack.OPTIONAL_CODEC, ItemStack.OPTIONAL_STREAM_CODEC);
	}

	public static Collection<EntityOverride<?>> getAllKeys() {
		return MAP.values();
	}

	public static final EntityOverride<Boolean> GLOWING = createBooleanKey("glowing");
	public static final EntityOverride<Color> TEAM_COLOR = createColorKey("team_color");
	public static final EntityOverride<Boolean> SUSPENDED = createBooleanKey("suspended");
	public static final EntityOverride<Double> GRAVITY = createDoubleKey("gravity");
	public static final EntityOverride<Float> SPEED = createFloatKey("speed");
	public static final EntityOverride<Boolean> PVP = createBooleanKey("pvp");
	public static final EntityOverride<Boolean> PASS_THROUGH_BARRIERS = createBooleanKey("pass_through_barriers");
	public static final EntityOverride<Integer> REGENERATE = createIntKey("regenerate");
	public static final EntityOverride<Boolean> INVULNERABLE = createBooleanKey("invulnerable");
	public static final EntityOverride<IconHolder> PLUMBOB = createKey("plumbob", IconHolder.CODEC, IconHolder.STREAM_CODEC);

	public final String id;
	private final Codec<T> codec;
	private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

	private EntityOverrideValue<T> all;
	private Map<EntityType<?>, EntityOverrideValue<T>> types;
	private List<Pair<EntityFilter, EntityOverrideValue<T>>> filtered;

	private EntityOverride(String id, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		this.id = id;
		this.codec = codec;
		this.streamCodec = streamCodec;
	}

	public String id() {
		return id;
	}

	public Codec<T> codec() {
		return codec;
	}

	public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
		return streamCodec;
	}

	@Override
	public String toString() {
		return id;
	}

	@Nullable
	public T get(@Nullable ShimmerEntity entity) {
		if (entity == null || entity.shimmer$isSaving()) {
			return null;
		}

		var v = entity.shimmer$getDirectOverride(this);

		if (v != null) {
			return v;
		}

		var e = (Entity) entity;

		if (e instanceof Player) {
			for (var instance : e.getZones()) {
				var v1 = instance.zone.playerOverrides().get(this);

				if (v1 != null) {
					return Cast.to(v1);
				}
			}
		}

		if (filtered != null) {
			for (var pair : filtered) {
				if (pair.getFirst().test(e)) {
					var v1 = pair.getSecond().get(e);

					if (v1 != null) {
						return v1;
					}
				}
			}
		}

		var t = types == null ? null : types.get(e.getType());

		if (t != null) {
			var v1 = t.get(e);

			if (v1 != null) {
				return v1;
			}
		}

		return all == null ? null : all.get(e);
	}

	public T get(@Nullable ShimmerEntity entity, T def) {
		var v = get(entity);
		return v == null ? def : v;
	}

	public void set(ShimmerEntity entity, @Nullable EntityOverrideValue<T> value) {
		entity.shimmer$setDirectOverride(this, value);
	}

	public void set(ShimmerEntity entity, T value) {
		set(entity, EntityOverrideValue.fixed(value));
	}

	public void setGlobal(EntityFilter filter, EntityOverrideValue<T> value) {
		if (filtered == null) {
			filtered = new ArrayList<>(1);
		}

		filtered.add(Pair.of(filter, Objects.requireNonNull(value)));
	}

	public void setGlobal(EntityFilter filter, T value) {
		setGlobal(filter, EntityOverrideValue.fixed(value));
	}

	public void setGlobal(EntityType<?> type, @Nullable EntityOverrideValue<T> value) {
		if (value != null) {
			if (types == null) {
				types = new IdentityHashMap<>(1);
			}

			types.put(type, value);
		} else if (types != null) {
			types.remove(type);

			if (types.isEmpty()) {
				types = null;
			}
		}
	}

	public void setGlobal(EntityType<?> type, T value) {
		setGlobal(type, EntityOverrideValue.fixed(value));
	}

	public void setGlobal(@Nullable EntityOverrideValue<T> value) {
		all = value;
	}

	public void setGlobal(T value) {
		setGlobal(EntityOverrideValue.fixed(value));
	}
}
