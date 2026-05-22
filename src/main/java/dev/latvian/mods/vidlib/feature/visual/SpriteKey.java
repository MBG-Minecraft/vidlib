package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class SpriteKey {
	public static final ResourceLocation SPECIAL = VidLib.id("special");
	public static final ResourceLocation BLOCKS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
	public static final ResourceLocation PARTICLES = ResourceLocation.withDefaultNamespace("textures/atlas/particles.png");
	public static final ResourceLocation GUI = ResourceLocation.withDefaultNamespace("textures/atlas/gui.png");

	public static final ResourceLocation MISSING_SPRITE = ResourceLocation.withDefaultNamespace("missingno");

	public static final SpriteKey EMPTY = new SpriteKey(1, SPECIAL, VidLibTextures.TRANSPARENT);
	public static final SpriteKey WHITE = special(VidLibTextures.SQUARE);
	private static final Map<ResourceLocation, ResourceLocation> INTERN_ATLAS = new HashMap<>();

	public static SpriteKey of(ResourceLocation atlas, ResourceLocation sprite) {
		int atlasType = 0;

		if (atlas == SPECIAL) {
			atlasType = 1;
		} else if (atlas == BLOCKS) {
			atlasType = 2;
		} else if (atlas == PARTICLES) {
			atlasType = 3;
		} else if (atlas == GUI) {
			atlasType = 4;
		} else if (atlas.equals(SPECIAL)) {
			atlas = SPECIAL;
			atlasType = 1;
		} else if (atlas.equals(BLOCKS)) {
			atlas = BLOCKS;
			atlasType = 2;
		} else if (atlas.equals(PARTICLES)) {
			atlas = PARTICLES;
			atlasType = 3;
		} else if (atlas.equals(GUI)) {
			atlas = GUI;
			atlasType = 4;
		} else {
			atlas = INTERN_ATLAS.computeIfAbsent(atlas, Function.identity());
		}

		return atlas == SPECIAL && sprite.equals(VidLibTextures.TRANSPARENT) ? EMPTY : new SpriteKey(atlasType, atlas, sprite);
	}

	public static SpriteKey special(ResourceLocation sprite) {
		return of(SPECIAL, sprite);
	}

	public static SpriteKey block(ResourceLocation sprite) {
		return of(BLOCKS, sprite);
	}

	public static SpriteKey particle(ResourceLocation sprite) {
		return of(PARTICLES, sprite);
	}

	public static SpriteKey gui(ResourceLocation sprite) {
		return of(GUI, sprite);
	}

	public static final Codec<SpriteKey> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.optionalFieldOf("atlas", BLOCKS).forGetter(SpriteKey::atlas),
		ID.CODEC.fieldOf("sprite").forGetter(SpriteKey::sprite)
	).apply(instance, SpriteKey::of));

	public static final Codec<SpriteKey> SPECIAL_CODEC = ID.CODEC.flatXmap(id -> DataResult.success(SpriteKey.special(id)), key -> key.isSpecial() ? DataResult.success(key.sprite()) : DataResult.error(() -> "Not a special type atlas sprite"));
	public static final Codec<SpriteKey> BLOCK_CODEC = ID.CODEC.flatXmap(id -> DataResult.success(SpriteKey.block(id)), key -> key.isBlock() ? DataResult.success(key.sprite()) : DataResult.error(() -> "Not a block type atlas sprite"));

	public static final Codec<SpriteKey> PREFER_SPECIAL_CODEC = KLibCodecs.or(SPECIAL_CODEC, MAP_CODEC);
	public static final Codec<SpriteKey> PREFER_BLOCK_CODEC = KLibCodecs.or(BLOCK_CODEC, MAP_CODEC);

	public static final StreamCodec<ByteBuf, SpriteKey> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public SpriteKey decode(ByteBuf buf) {
			int atlasType = buf.readByte();

			if (atlasType == 1) {
				return new SpriteKey(1, SPECIAL, ResourceLocation.STREAM_CODEC.decode(buf));
			} else if (atlasType == 2) {
				return new SpriteKey(2, BLOCKS, ResourceLocation.STREAM_CODEC.decode(buf));
			} else if (atlasType == 3) {
				return new SpriteKey(3, PARTICLES, ResourceLocation.STREAM_CODEC.decode(buf));
			} else if (atlasType == 4) {
				return new SpriteKey(4, GUI, ResourceLocation.STREAM_CODEC.decode(buf));
			} else {
				return new SpriteKey(atlasType, INTERN_ATLAS.computeIfAbsent(ResourceLocation.STREAM_CODEC.decode(buf), Function.identity()), ResourceLocation.STREAM_CODEC.decode(buf));
			}
		}

		@Override
		public void encode(ByteBuf buf, SpriteKey value) {
			buf.writeByte(value.atlasType);

			if (value.atlasType == 0) {
				ResourceLocation.STREAM_CODEC.encode(buf, value.atlas);
			}

			ResourceLocation.STREAM_CODEC.encode(buf, value.sprite);
		}
	};

	public static final StreamCodec<ByteBuf, Optional<SpriteKey>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);

	private final int atlasType;
	private final ResourceLocation atlas;
	private final ResourceLocation sprite;

	private SpriteKey(int atlasType, ResourceLocation atlas, ResourceLocation sprite) {
		this.atlasType = atlasType;
		this.atlas = atlas;
		this.sprite = sprite;
	}

	public ResourceLocation atlas() {
		return atlas;
	}

	public ResourceLocation sprite() {
		return sprite;
	}

	public ResourceLocation dynamic() {
		return ResourceLocation.fromNamespaceAndPath(sprite.getNamespace(), "textures/vidlib/generated/atlas/" + atlas.getNamespace() + "/" + atlas.getPath() + "/" + sprite.getPath() + ".png");
	}

	@Override
	@NotNull
	public String toString() {
		return atlas + ":" + sprite;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof SpriteKey k) {
			return atlas == k.atlas && sprite.equals(k.sprite);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(atlas, sprite);
	}

	public boolean isSpecial() {
		return atlas == SPECIAL;
	}

	public boolean isBlock() {
		return atlas == BLOCKS;
	}

	public boolean isParticle() {
		return atlas == PARTICLES;
	}

	public boolean isGui() {
		return atlas == GUI;
	}

	public ResourceLocation getTexture() {
		return isSpecial() ? sprite : atlas;
	}
}
