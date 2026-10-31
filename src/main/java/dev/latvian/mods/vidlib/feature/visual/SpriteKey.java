package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecErrors;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

public final class SpriteKey {
	public static final Identifier MISSING_ATLAS = ID.vidlib("missing");
	public static final Identifier SPECIAL_ATLAS = ID.vidlib("special");
	public static final Identifier MISSING_SPRITE = ID.mc("missingno");
	public static final SpriteKey EMPTY = new SpriteKey(1, MISSING_ATLAS, MISSING_SPRITE);

	private static final Reference2IntMap<Identifier> ATLAS_TO_ID = new Reference2IntOpenHashMap<>();
	private static final Int2ObjectMap<Identifier> ID_TO_ATLAS = new Int2ObjectOpenHashMap<>();
	private static final Map<Identifier, Identifier> INTERN_ATLAS = new Object2ObjectOpenHashMap<>();

	static {
		// custom atlas = 0
		// EMPTY instance = 1
		ATLAS_TO_ID.put(MISSING_ATLAS, 2);
		ATLAS_TO_ID.put(SPECIAL_ATLAS, 3);
		ATLAS_TO_ID.put(AtlasIds.BLOCKS, 4);
		ATLAS_TO_ID.put(AtlasIds.ITEMS, 5);
		ATLAS_TO_ID.put(AtlasIds.GUI, 6);
		ATLAS_TO_ID.put(AtlasIds.PARTICLES, 7);
		ATLAS_TO_ID.put(AtlasIds.CHESTS, 8);
		ATLAS_TO_ID.put(AtlasIds.PAINTINGS, 9);
		ATLAS_TO_ID.put(AtlasIds.SHIELD_PATTERNS, 10);
		ATLAS_TO_ID.put(AtlasIds.MAP_DECORATIONS, 11);
		ATLAS_TO_ID.put(AtlasIds.DECORATED_POT, 12);
		ATLAS_TO_ID.put(AtlasIds.BANNER_PATTERNS, 13);
		ATLAS_TO_ID.put(AtlasIds.SHULKER_BOXES, 14);
		ATLAS_TO_ID.put(AtlasIds.CELESTIALS, 15);

		for (var entry : ATLAS_TO_ID.reference2IntEntrySet()) {
			ID_TO_ATLAS.put(entry.getIntValue(), entry.getKey());
			INTERN_ATLAS.put(entry.getKey(), entry.getKey());
		}

		INTERN_ATLAS.put(ID.mc("textures/atlas/banner_patterns.png"), AtlasIds.BANNER_PATTERNS);
		INTERN_ATLAS.put(ID.mc("textures/atlas/blocks.png"), AtlasIds.BLOCKS);
		INTERN_ATLAS.put(ID.mc("textures/atlas/items.png"), AtlasIds.ITEMS);
		INTERN_ATLAS.put(ID.mc("textures/atlas/chest.png"), AtlasIds.CHESTS);
		INTERN_ATLAS.put(ID.mc("textures/atlas/decorated_pot.png"), AtlasIds.DECORATED_POT);
		INTERN_ATLAS.put(ID.mc("textures/atlas/gui.png"), AtlasIds.GUI);
		INTERN_ATLAS.put(ID.mc("textures/atlas/map_decorations.png"), AtlasIds.MAP_DECORATIONS);
		INTERN_ATLAS.put(ID.mc("textures/atlas/paintings.png"), AtlasIds.PAINTINGS);
		INTERN_ATLAS.put(ID.mc("textures/atlas/particles.png"), AtlasIds.PARTICLES);
		INTERN_ATLAS.put(ID.mc("textures/atlas/shield_patterns.png"), AtlasIds.SHIELD_PATTERNS);
		INTERN_ATLAS.put(ID.mc("textures/atlas/shulker_boxes.png"), AtlasIds.SHULKER_BOXES);
		INTERN_ATLAS.put(ID.mc("textures/atlas/celestials.png"), AtlasIds.CELESTIALS);
	}

	public static Identifier internAtlas(Identifier atlas) {
		return INTERN_ATLAS.computeIfAbsent(atlas, Function.identity());
	}

	public static final SpriteKey WHITE = special(VidLibTextures.WHITE.texturePath());

	public static SpriteKey of(Identifier atlas, Identifier sprite) {
		int atlasType = ATLAS_TO_ID.getOrDefault(atlas, 0);

		if (atlasType == 0) {
			atlas = internAtlas(atlas);
			atlasType = ATLAS_TO_ID.getOrDefault(atlas, 0);
		}

		if (atlas == MISSING_ATLAS) {
			return missing(sprite);
		} else {
			return new SpriteKey(atlasType, atlas, sprite);
		}
	}

	public static SpriteKey missing(Identifier sprite) {
		return sprite.equals(EMPTY.sprite) ? EMPTY : new SpriteKey(2, MISSING_ATLAS, sprite);
	}

	public static SpriteKey special(Identifier sprite) {
		return new SpriteKey(3, SPECIAL_ATLAS, sprite);
	}

	public static SpriteKey block(Identifier sprite) {
		return new SpriteKey(4, AtlasIds.BLOCKS, sprite);
	}

	public static SpriteKey item(Identifier sprite) {
		return new SpriteKey(5, AtlasIds.ITEMS, sprite);
	}

	public static SpriteKey gui(Identifier sprite) {
		return new SpriteKey(6, AtlasIds.GUI, sprite);
	}

	public static SpriteKey particle(Identifier sprite) {
		return new SpriteKey(7, AtlasIds.PARTICLES, sprite);
	}

	public static SpriteKey chest(Identifier sprite) {
		return new SpriteKey(8, AtlasIds.CHESTS, sprite);
	}

	public static SpriteKey painting(Identifier sprite) {
		return new SpriteKey(9, AtlasIds.PAINTINGS, sprite);
	}

	public static SpriteKey shieldPattern(Identifier sprite) {
		return new SpriteKey(10, AtlasIds.SHIELD_PATTERNS, sprite);
	}

	public static SpriteKey mapDecoration(Identifier sprite) {
		return new SpriteKey(11, AtlasIds.MAP_DECORATIONS, sprite);
	}

	public static SpriteKey decoratedPot(Identifier sprite) {
		return new SpriteKey(12, AtlasIds.DECORATED_POT, sprite);
	}

	public static SpriteKey bannerPattern(Identifier sprite) {
		return new SpriteKey(13, AtlasIds.BANNER_PATTERNS, sprite);
	}

	public static SpriteKey shulkerBox(Identifier sprite) {
		return new SpriteKey(14, AtlasIds.SHULKER_BOXES, sprite);
	}

	public static SpriteKey celestial(Identifier sprite) {
		return new SpriteKey(15, AtlasIds.CELESTIALS, sprite);
	}

	public static final MapCodec<SpriteKey> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ID.CODEC.optionalFieldOf("atlas", AtlasIds.BLOCKS).forGetter(SpriteKey::atlas),
		ID.CODEC.fieldOf("sprite").forGetter(SpriteKey::sprite)
	).apply(instance, SpriteKey::of));

	public static final Codec<SpriteKey> CODEC = MAP_CODEC.codec();

	private static final DataResult<Identifier> SPECIAL_CODEC_ERROR = KLibCodecErrors.error("Not a special type atlas sprite");
	private static final DataResult<Identifier> BLOCK_CODEC_ERROR = KLibCodecErrors.error("Not a block type atlas sprite");
	private static final DataResult<Identifier> ITEM_CODEC_ERROR = KLibCodecErrors.error("Not an item type atlas sprite");

	public static final Codec<SpriteKey> SPECIAL_CODEC = ID.CODEC.flatComapMap(SpriteKey::special, key -> key.isSpecial() ? DataResult.success(key.sprite()) : SPECIAL_CODEC_ERROR);
	public static final Codec<SpriteKey> BLOCK_CODEC = ID.CODEC.flatComapMap(SpriteKey::block, key -> key.isBlock() ? DataResult.success(key.sprite()) : BLOCK_CODEC_ERROR);
	public static final Codec<SpriteKey> ITEM_CODEC = ID.CODEC.flatComapMap(SpriteKey::item, key -> key.isItem() ? DataResult.success(key.sprite()) : ITEM_CODEC_ERROR);

	public static final Codec<SpriteKey> PREFER_SPECIAL_CODEC = KLibCodecs.or(SPECIAL_CODEC, CODEC);
	public static final Codec<SpriteKey> PREFER_BLOCK_CODEC = KLibCodecs.or(BLOCK_CODEC, CODEC);

	public static final StreamCodec<ByteBuf, SpriteKey> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public SpriteKey decode(ByteBuf buf) {
			int atlasType = VarInt.read(buf);

			if (atlasType == 0) {
				var atlas = internAtlas(ID.STREAM_CODEC.decode(buf));
				var sprite = ID.STREAM_CODEC.decode(buf);
				return new SpriteKey(atlasType, atlas, sprite);
			} else if (atlasType == 1) {
				return EMPTY;
			} else {
				var atlas = ID_TO_ATLAS.get(atlasType);

				if (atlas == null) {
					throw new NullPointerException("Invalid atlas type " + atlasType);
				}

				var sprite = ID.STREAM_CODEC.decode(buf);
				return new SpriteKey(atlasType, atlas, sprite);
			}
		}

		@Override
		public void encode(ByteBuf buf, SpriteKey value) {
			if (value == EMPTY || value.isMissing() && value.sprite.equals(EMPTY.sprite)) {
				VarInt.write(buf, 1);
			} else {
				VarInt.write(buf, value.atlasType);

				if (value.atlasType == 0) {
					ID.STREAM_CODEC.encode(buf, value.atlas);
				}

				ID.STREAM_CODEC.encode(buf, value.sprite);
			}
		}
	};

	public static final StreamCodec<ByteBuf, SpriteKey> OPTIONAL_STREAM_CODEC = KLibStreamCodecs.optional(STREAM_CODEC, EMPTY);

	private final int atlasType;
	private final Identifier atlas;
	private final Identifier sprite;

	private SpriteKey(int atlasType, Identifier atlas, Identifier sprite) {
		this.atlasType = atlasType;
		this.atlas = atlas;
		this.sprite = sprite;
	}

	public Identifier atlas() {
		return atlas;
	}

	public Identifier sprite() {
		return sprite;
	}

	public Identifier dynamic() {
		return sprite.withPath("textures/vidlib/generated/atlas/" + atlas.getNamespace() + "/" + atlas.getPath() + "/" + sprite.getPath() + ".png");
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
		return atlas.hashCode() ^ sprite.hashCode();
	}

	public boolean isMissing() {
		return atlas == MISSING_ATLAS;
	}

	public boolean isSpecial() {
		return atlas == SPECIAL_ATLAS;
	}

	public boolean isBlock() {
		return atlas == AtlasIds.BLOCKS;
	}

	public boolean isItem() {
		return atlas == AtlasIds.ITEMS;
	}

	public boolean isGui() {
		return atlas == AtlasIds.GUI;
	}

	public boolean isParticle() {
		return atlas == AtlasIds.PARTICLES;
	}

	public Identifier getTexture() {
		return isSpecial() ? sprite : atlas.withPath("textures/atlas/" + atlas.getPath() + ".png");
	}
}
