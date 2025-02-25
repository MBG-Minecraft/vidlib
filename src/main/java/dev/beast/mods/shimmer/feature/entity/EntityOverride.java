package dev.beast.mods.shimmer.feature.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.core.ShimmerEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class EntityOverride<T> {
	private static final Map<String, EntityOverride<?>> MAP = new HashMap<>();

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

	public static EntityOverride<Integer> createVarIntKey(String id) {
		return createKey(id, Codec.INT, ByteBufCodecs.VAR_INT);
	}

	public static Collection<EntityOverride<?>> getAllKeys() {
		return MAP.values();
	}

	public static final EntityOverride<Boolean> GLOWING = createBooleanKey("glowing");
	public static final EntityOverride<Integer> TEAM_COLOR = createIntKey("team_color");
	public static final EntityOverride<Boolean> AI = createBooleanKey("ai");

	public final String id;
	private final Codec<T> codec;
	private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

	private T all;
	private Map<EntityType<?>, T> types;
	private List<Pair<Predicate<Entity>, T>> predicates;

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
	public T get(ShimmerEntity entity) {
		if (entity.shimmer$isSaving()) {
			return null;
		}

		var v = entity.shimmer$getDirectOverride(this);

		if (v != null) {
			return v;
		}

		var e = (Entity) entity;
		var t = types == null ? null : types.get(e.getType());

		if (t != null) {
			return t;
		}

		if (predicates != null) {
			for (var pair : predicates) {
				if (pair.getFirst().test(e)) {
					return pair.getSecond();
				}
			}
		}

		return all;
	}

	public void set(ShimmerEntity entity, @Nullable T value) {
		entity.shimmer$setDirectOverride(this, value);
	}

	public void setGlobal(Predicate<Entity> predicate, @Nullable T value) {
		if (predicates == null) {
			predicates = new ArrayList<>(1);
		}

		predicates.add(Pair.of(predicate, value));
	}

	public void setGlobal(EntityType<?> type, @Nullable T value) {
		if (types == null) {
			types = new IdentityHashMap<>(1);
		}

		types.put(type, value);
	}

	public void setGlobal(@Nullable T value) {
		all = value;
	}
}
