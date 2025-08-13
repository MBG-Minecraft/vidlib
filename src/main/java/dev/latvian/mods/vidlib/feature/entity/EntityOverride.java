package dev.latvian.mods.vidlib.feature.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.math.Range;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.core.VLEntity;
import dev.latvian.mods.vidlib.core.VLPlayer;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.hud.SpectatorTablistOverride;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.feature.skybox.FogOverride;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoInit
public final class EntityOverride<T> {
	private static final Map<String, EntityOverride<?>> MAP = new HashMap<>();

	public static final Codec<EntityOverride<?>> CODEC = KLibCodecs.map(() -> MAP, Codec.STRING, EntityOverride::id);
	public static final Codec<Map<EntityOverride<?>, Object>> OVERRIDE_MAP_CODEC = Codec.dispatchedMap(CODEC, o -> o.type.codec());

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
				var value = key.type.streamCodec().decode(buf);
				map.put(key, value);
			}

			return map;
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, Map<EntityOverride<?>, Object> map) {
			buf.writeVarInt(map.size());

			for (var entry : map.entrySet()) {
				buf.writeUtf(entry.getKey().id());
				entry.getKey().type.streamCodec().encode(buf, Cast.to(entry.getValue()));
			}
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> EntityOverride<T> createKey(String id, DataType<T> type) {
		return (EntityOverride<T>) MAP.computeIfAbsent(id, k -> new EntityOverride<>(k, type));
	}

	public static EntityOverride<Boolean> createBooleanKey(String id) {
		return createKey(id, DataTypes.BOOL);
	}

	public static EntityOverride<Integer> createIntKey(String id) {
		return createKey(id, DataTypes.INT);
	}

	public static EntityOverride<Color> createColorKey(String id) {
		return createKey(id, Color.DATA_TYPE);
	}

	public static EntityOverride<Integer> createVarIntKey(String id) {
		return createKey(id, DataTypes.VAR_INT);
	}

	public static EntityOverride<Float> createFloatKey(String id) {
		return createKey(id, DataTypes.FLOAT);
	}

	public static EntityOverride<Double> createDoubleKey(String id) {
		return createKey(id, DataTypes.DOUBLE);
	}

	public static EntityOverride<ItemStack> createItemKey(String id) {
		return createKey(id, DataTypes.ITEM_STACK);
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
	public static final EntityOverride<IconHolder> PLUMBOB = createKey("plumbob", IconHolder.DATA_TYPE);
	public static final EntityOverride<Float> ATTACK_DAMAGE = createFloatKey("attack_damage");
	public static final EntityOverride<Clothing> CLOTHING = createKey("clothing", Clothing.DATA_TYPE);
	public static final EntityOverride<ResourceLocation> SKYBOX = createKey("skybox", ID.DATA_TYPE);
	public static final EntityOverride<Range> AMBIENT_LIGHT = createKey("ambient_light", Range.DATA_TYPE);
	public static final EntityOverride<FogOverride> FOG = createKey("fog", FogOverride.DATA_TYPE);
	public static final EntityOverride<FogOverride> FLUID_FOG = createKey("fluid_fog", FogOverride.DATA_TYPE);
	public static final EntityOverride<Boolean> UNPUSHABLE = createBooleanKey("unpushable");
	public static final EntityOverride<Component> NICKNAME = createKey("nickname", DataTypes.TEXT_COMPONENT);
	public static final EntityOverride<Component> NAME_PREFIX = createKey("name_prefix", DataTypes.TEXT_COMPONENT);
	public static final EntityOverride<Component> NAME_SUFFIX = createKey("name_suffix", DataTypes.TEXT_COMPONENT);
	public static final EntityOverride<Component> SCORE_TEXT = createKey("score_text", DataTypes.TEXT_COMPONENT);
	public static final EntityOverride<Boolean> NAME_HIDDEN = createBooleanKey("name_hidden");
	public static final EntityOverride<List<ChancedParticle>> ENVIRONMENT_EFFECTS = createKey("environment_effects", ChancedParticle.LIST_DATA_TYPE);
	public static final EntityOverride<Boolean> SCALE_DAMAGE_WITH_DIFFICULTY = createBooleanKey("scale_damage_with_difficulty");
	public static final EntityOverride<SpectatorTablistOverride.TablistOverrideType> SHOW_SPECTATORS_TABLIST =
		createKey("show_spectators_tablist", SpectatorTablistOverride.DATA_TYPE);

	public final String id;
	private final DataType<T> type;

	EntityOverrideValue<T> all;
	Map<EntityType<?>, EntityOverrideValue<T>> types;
	List<Pair<EntityFilter, EntityOverrideValue<T>>> filtered;

	private EntityOverride(String id, DataType<T> type) {
		this.id = id;
		this.type = type;
	}

	public String id() {
		return id;
	}

	public DataType<T> type() {
		return type;
	}

	@Override
	public String toString() {
		return id;
	}

	@Nullable
	public T get(@Nullable VLEntity entity) {
		if (entity == null || entity.vl$isSaving()) {
			return null;
		}

		var v = entity.vl$getDirectOverride(this);

		if (v != null) {
			return v;
		}

		var e = (Entity) entity;

		if (e instanceof Player) {
			var f = ForcedPlayerOverrides.MAP.get(e.getUUID());

			if (f != null) {
				var fo = f.get(this);

				if (fo != null) {
					return Cast.to(fo);
				}
			}

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

	public T get(@Nullable VLEntity entity, T def) {
		var v = get(entity);
		return v == null ? def : v;
	}

	public T get(VLPlayer player, @Nullable T def, DataKey<T> playerDataFallback) {
		var v = get(player);
		return v == null || v.equals(def) ? player.get(playerDataFallback) : v;
	}

	public void set(VLEntity entity, @Nullable EntityOverrideValue<T> value) {
		entity.vl$setDirectOverride(this, value);
	}

	public void set(VLEntity entity, T value) {
		set(entity, EntityOverrideValue.fixed(value));
	}
}
