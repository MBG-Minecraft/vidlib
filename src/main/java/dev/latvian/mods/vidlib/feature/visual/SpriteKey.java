package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SpriteKey {
	public static final ClientAsset.ResourceTexture SPECIAL = new ClientAsset.ResourceTexture(ID.vidlib("special"));
	public static final ClientAsset.ResourceTexture BLOCKS = new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("atlas/blocks"));
	public static final ClientAsset.ResourceTexture PARTICLES = new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("atlas/particles"));
	public static final ClientAsset.ResourceTexture GUI = new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("atlas/gui"));

	public static final ClientAsset.ResourceTexture MISSING_SPRITE = new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("missingno"));

	public static final SpriteKey EMPTY = new SpriteKey(1, SPECIAL, narrow(VidLibTextures.TRANSPARENT));
	public static final SpriteKey WHITE = special(narrow(VidLibTextures.SQUARE));
	private static final Map<Identifier, ClientAsset.ResourceTexture> INTERN_ATLAS = new HashMap<>();

	/**
	 * 26.3 turned {@code ClientAsset} into an interface with {@code ResourceTexture} /
	 * {@code DownloadedTexture} variants. VidLib only deals with resource textures, so anything
	 * else is normalised here.
	 */
	public static ClientAsset.ResourceTexture narrow(ClientAsset asset) {
		return asset instanceof ClientAsset.ResourceTexture resourceTexture ? resourceTexture : new ClientAsset.ResourceTexture(asset.id());
	}

	public static SpriteKey of(ClientAsset atlasAsset, ClientAsset spriteAsset) {
		var sprite = narrow(spriteAsset);
		ClientAsset.ResourceTexture atlas;
		int atlasType;

		if (atlasAsset == SPECIAL) {
			atlas = SPECIAL;
			atlasType = 1;
		} else if (atlasAsset == BLOCKS) {
			atlas = BLOCKS;
			atlasType = 2;
		} else if (atlasAsset == PARTICLES) {
			atlas = PARTICLES;
			atlasType = 3;
		} else if (atlasAsset == GUI) {
			atlas = GUI;
			atlasType = 4;
		} else if (atlasAsset.equals(SPECIAL)) {
			atlas = SPECIAL;
			atlasType = 1;
		} else if (atlasAsset.equals(BLOCKS)) {
			atlas = BLOCKS;
			atlasType = 2;
		} else if (atlasAsset.equals(PARTICLES)) {
			atlas = PARTICLES;
			atlasType = 3;
		} else if (atlasAsset.equals(GUI)) {
			atlas = GUI;
			atlasType = 4;
		} else {
			atlas = INTERN_ATLAS.computeIfAbsent(atlasAsset.id(), ClientAsset.ResourceTexture::new);
			atlasType = 0;
		}

		return atlas == SPECIAL && sprite.id().equals(VidLibTextures.TRANSPARENT.id()) ? EMPTY : new SpriteKey(atlasType, atlas, sprite);
	}

	public static SpriteKey special(ClientAsset sprite) {
		return of(SPECIAL, narrow(sprite));
	}

	public static SpriteKey block(ClientAsset sprite) {
		return of(BLOCKS, narrow(sprite));
	}

	public static SpriteKey particle(ClientAsset sprite) {
		return of(PARTICLES, narrow(sprite));
	}

	public static SpriteKey gui(ClientAsset sprite) {
		return of(GUI, narrow(sprite));
	}

	public static final Codec<SpriteKey> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ClientAsset.ResourceTexture.CODEC.optionalFieldOf("atlas", BLOCKS).forGetter(SpriteKey::atlas),
		ClientAsset.ResourceTexture.CODEC.fieldOf("sprite").forGetter(SpriteKey::sprite)
	).apply(instance, SpriteKey::of));

	public static final Codec<SpriteKey> SPECIAL_CODEC = ClientAsset.ResourceTexture.CODEC.flatXmap(id -> DataResult.success(SpriteKey.special(id)), key -> key.isSpecial() ? DataResult.success(key.sprite()) : DataResult.error(() -> "Not a special type atlas sprite"));
	public static final Codec<SpriteKey> BLOCK_CODEC = ClientAsset.ResourceTexture.CODEC.flatXmap(id -> DataResult.success(SpriteKey.block(id)), key -> key.isBlock() ? DataResult.success(key.sprite()) : DataResult.error(() -> "Not a block type atlas sprite"));

	public static final Codec<SpriteKey> PREFER_SPECIAL_CODEC = KLibCodecs.or(SPECIAL_CODEC, MAP_CODEC);
	public static final Codec<SpriteKey> PREFER_BLOCK_CODEC = KLibCodecs.or(BLOCK_CODEC, MAP_CODEC);

	public static final StreamCodec<ByteBuf, SpriteKey> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public SpriteKey decode(ByteBuf buf) {
			int atlasType = buf.readByte();

			if (atlasType == 1) {
				return new SpriteKey(1, SPECIAL, narrow(ClientAsset.ResourceTexture.STREAM_CODEC.decode(buf)));
			} else if (atlasType == 2) {
				return new SpriteKey(2, BLOCKS, narrow(ClientAsset.ResourceTexture.STREAM_CODEC.decode(buf)));
			} else if (atlasType == 3) {
				return new SpriteKey(3, PARTICLES, narrow(ClientAsset.ResourceTexture.STREAM_CODEC.decode(buf)));
			} else if (atlasType == 4) {
				return new SpriteKey(4, GUI, narrow(ClientAsset.ResourceTexture.STREAM_CODEC.decode(buf)));
			} else {
				return new SpriteKey(atlasType, INTERN_ATLAS.computeIfAbsent(ID.STREAM_CODEC.decode(buf), ClientAsset.ResourceTexture::new), narrow(ClientAsset.ResourceTexture.STREAM_CODEC.decode(buf)));
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
	private final ClientAsset.ResourceTexture atlas;
	private final ClientAsset.ResourceTexture sprite;

	private SpriteKey(int atlasType, ClientAsset.ResourceTexture atlas, ClientAsset.ResourceTexture sprite) {
		this.atlasType = atlasType;
		this.atlas = atlas;
		this.sprite = sprite;
	}

	public ClientAsset.ResourceTexture atlas() {
		return atlas;
	}

	public ClientAsset.ResourceTexture sprite() {
		return sprite;
	}

	public Identifier dynamic() {
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

	public ClientAsset.ResourceTexture getTexture() {
		return isSpecial() ? sprite : atlas;
	}
}
