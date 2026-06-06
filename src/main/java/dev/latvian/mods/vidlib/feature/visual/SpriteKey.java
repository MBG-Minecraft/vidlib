package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SpriteKey {
	public static final ClientAsset SPECIAL = new ClientAsset(VidLib.id("special"));
	public static final ClientAsset BLOCKS = new ClientAsset(ResourceLocation.withDefaultNamespace("atlas/blocks"));
	public static final ClientAsset PARTICLES = new ClientAsset(ResourceLocation.withDefaultNamespace("atlas/particles"));
	public static final ClientAsset GUI = new ClientAsset(ResourceLocation.withDefaultNamespace("atlas/gui"));

	public static final ClientAsset MISSING_SPRITE = new ClientAsset(ResourceLocation.withDefaultNamespace("missingno"));

	public static final SpriteKey EMPTY = new SpriteKey(1, SPECIAL, VidLibTextures.TRANSPARENT);
	public static final SpriteKey WHITE = special(VidLibTextures.SQUARE);
	private static final Map<ResourceLocation, ClientAsset> INTERN_ATLAS = new HashMap<>();

	public static SpriteKey of(ClientAsset atlas, ClientAsset sprite) {
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
			atlas = INTERN_ATLAS.computeIfAbsent(atlas.id(), ClientAsset::new);
		}

		return atlas == SPECIAL && sprite.equals(VidLibTextures.TRANSPARENT) ? EMPTY : new SpriteKey(atlasType, atlas, sprite);
	}

	public static SpriteKey special(ClientAsset sprite) {
		return of(SPECIAL, sprite);
	}

	public static SpriteKey block(ClientAsset sprite) {
		return of(BLOCKS, sprite);
	}

	public static SpriteKey particle(ClientAsset sprite) {
		return of(PARTICLES, sprite);
	}

	public static SpriteKey gui(ClientAsset sprite) {
		return of(GUI, sprite);
	}

	public static final Codec<SpriteKey> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ClientAsset.CODEC.optionalFieldOf("atlas", BLOCKS).forGetter(SpriteKey::atlas),
		ClientAsset.CODEC.fieldOf("sprite").forGetter(SpriteKey::sprite)
	).apply(instance, SpriteKey::of));

	public static final Codec<SpriteKey> SPECIAL_CODEC = ClientAsset.CODEC.flatXmap(id -> DataResult.success(SpriteKey.special(id)), key -> key.isSpecial() ? DataResult.success(key.sprite()) : DataResult.error(() -> "Not a special type atlas sprite"));
	public static final Codec<SpriteKey> BLOCK_CODEC = ClientAsset.CODEC.flatXmap(id -> DataResult.success(SpriteKey.block(id)), key -> key.isBlock() ? DataResult.success(key.sprite()) : DataResult.error(() -> "Not a block type atlas sprite"));

	public static final Codec<SpriteKey> PREFER_SPECIAL_CODEC = KLibCodecs.or(SPECIAL_CODEC, MAP_CODEC);
	public static final Codec<SpriteKey> PREFER_BLOCK_CODEC = KLibCodecs.or(BLOCK_CODEC, MAP_CODEC);

	public static final StreamCodec<ByteBuf, SpriteKey> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public SpriteKey decode(ByteBuf buf) {
			int atlasType = buf.readByte();

			if (atlasType == 1) {
				return new SpriteKey(1, SPECIAL, ClientAsset.STREAM_CODEC.decode(buf));
			} else if (atlasType == 2) {
				return new SpriteKey(2, BLOCKS, ClientAsset.STREAM_CODEC.decode(buf));
			} else if (atlasType == 3) {
				return new SpriteKey(3, PARTICLES, ClientAsset.STREAM_CODEC.decode(buf));
			} else if (atlasType == 4) {
				return new SpriteKey(4, GUI, ClientAsset.STREAM_CODEC.decode(buf));
			} else {
				return new SpriteKey(atlasType, INTERN_ATLAS.computeIfAbsent(ID.STREAM_CODEC.decode(buf), ClientAsset::new), ClientAsset.STREAM_CODEC.decode(buf));
			}
		}

		@Override
		public void encode(ByteBuf buf, SpriteKey value) {
			buf.writeByte(value.atlasType);

			if (value.atlasType == 0) {
				ID.STREAM_CODEC.encode(buf, value.atlas.id());
			}

			ID.STREAM_CODEC.encode(buf, value.sprite.id());
		}
	};

	public static final StreamCodec<ByteBuf, Optional<SpriteKey>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);

	private final int atlasType;
	private final ClientAsset atlas;
	private final ClientAsset sprite;

	private SpriteKey(int atlasType, ClientAsset atlas, ClientAsset sprite) {
		this.atlasType = atlasType;
		this.atlas = atlas;
		this.sprite = sprite;
	}

	public ClientAsset atlas() {
		return atlas;
	}

	public ClientAsset sprite() {
		return sprite;
	}

	public ResourceLocation dynamic() {
		return sprite.id().withPath("textures/vidlib/generated/atlas/" + atlas.id().getNamespace() + "/" + atlas.id().getPath() + "/" + sprite.id().getPath() + ".png");
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
		return Objects.hash(atlas, sprite.id());
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

	public ClientAsset getTexture() {
		return isSpecial() ? sprite : atlas;
	}
}
