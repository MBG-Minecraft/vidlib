package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.data.DataType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PlayerClothing(Type type, @Nullable ClothingSet custom, @Nullable ResourceKey<ClothingSet> preset) {
	public enum Type implements StringRepresentable {
		NONE("none", "None"),
		PRESET("preset", "Preset"),
		CUSTOM("custom", "Custom");

		public static final Type[] VALUES = values();

		public final String name;
		public final String displayName;

		Type(String name, String displayName) {
			this.name = name;
			this.displayName = displayName;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public static final PlayerClothing NONE = new PlayerClothing(Type.NONE, null, null);

	public static PlayerClothing custom(@Nullable ClothingSet clothingSet) {
		return clothingSet == null ? NONE : new PlayerClothing(Type.CUSTOM, clothingSet, null);
	}

	public static PlayerClothing preset(@Nullable ResourceKey<ClothingSet> preset) {
		return preset == null || preset == Clothing.NONE ? NONE : new PlayerClothing(Type.PRESET, null, preset);
	}

	public static final Codec<PlayerClothing> NONE_CODEC = KLibCodecs.unit("", NONE, playerClothing -> playerClothing.custom == null && playerClothing.preset == null);

	public static final Codec<PlayerClothing> CUSTOM_CODEC = ClothingSet.CODEC.flatComapMap(PlayerClothing::custom, playerClothing -> {
		if (playerClothing.custom != null) {
			return DataResult.success(playerClothing.custom);
		} else {
			return DataResult.error(() -> "Tried to convert preset clothing to custom clothing");
		}
	});

	public static final Codec<PlayerClothing> PRESET_CODEC = ClothingPresets.KEY_CODEC.flatComapMap(PlayerClothing::preset, playerClothing -> {
		if (playerClothing.preset != null) {
			return DataResult.success(playerClothing.preset);
		} else {
			return DataResult.error(() -> "Tried to convert custom clothing to preset clothing");
		}
	});

	public static final Codec<PlayerClothing> CODEC = KLibCodecs.or(List.of(NONE_CODEC, PRESET_CODEC, CUSTOM_CODEC));

	public static final StreamCodec<ByteBuf, PlayerClothing> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public PlayerClothing decode(ByteBuf buf) {
			return switch (buf.readByte()) {
				case 1 -> custom(ClothingSet.STREAM_CODEC.decode(buf));
				case 2 -> preset(ClothingPresets.KEY_STREAM_CODEC.decode(buf));
				default -> NONE;
			};
		}

		@Override
		public void encode(ByteBuf buf, PlayerClothing value) {
			if (value.custom != null) {
				buf.writeByte(1);
				ClothingSet.STREAM_CODEC.encode(buf, value.custom);
			} else if (value.preset != null) {
				buf.writeByte(2);
				ClothingPresets.KEY_STREAM_CODEC.encode(buf, value.preset);
			} else {
				buf.writeByte(0);
			}
		}
	};

	public static final DataType<PlayerClothing> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC);

	public PlayerClothing(Type type, @Nullable ClothingSet custom, @Nullable ResourceKey<ClothingSet> preset) {
		this.type = type;
		this.custom = custom;
		this.preset = preset == Clothing.NONE ? null : preset;
	}

	public ClothingSet resolve() {
		if (custom != null) {
			return custom;
		} else if (preset != null) {
			return ClothingPresets.INSTANCE.map.getOrDefault(preset, ClothingSet.EMPTY);
		} else {
			return ClothingSet.EMPTY;
		}
	}
}
