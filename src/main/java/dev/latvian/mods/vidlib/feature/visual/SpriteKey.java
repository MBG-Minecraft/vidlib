package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public record SpriteKey(ResourceLocation atlas, ResourceLocation sprite) {
	public static final ResourceLocation SPECIAL = VidLib.id("special");
	public static final ResourceLocation BLOCKS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
	public static final ResourceLocation PARTICLES = ResourceLocation.withDefaultNamespace("textures/atlas/particles.png");
	public static final SpriteKey EMPTY = new SpriteKey(SPECIAL, Empty.ID);
	public static final SpriteKey WHITE = special(ResourceLocation.withDefaultNamespace("textures/misc/white.png"));

	public static ResourceLocation atlasOf(ResourceLocation id) {
		if (id == SPECIAL || id == BLOCKS || id == PARTICLES) {
			return id;
		} else if (id.equals(SPECIAL)) {
			return SPECIAL;
		} else if (id.equals(BLOCKS)) {
			return BLOCKS;
		} else if (id.equals(PARTICLES)) {
			return PARTICLES;
		} else {
			return id;
		}
	}

	public static SpriteKey of(ResourceLocation atlas, ResourceLocation sprite) {
		var a = atlasOf(atlas);
		return a == SPECIAL && sprite.equals(Empty.ID) ? EMPTY : new SpriteKey(a, sprite);
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

	public static final Codec<SpriteKey> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.optionalFieldOf("atlas", BLOCKS).forGetter(SpriteKey::atlas),
		ID.CODEC.fieldOf("sprite").forGetter(SpriteKey::sprite)
	).apply(instance, SpriteKey::of));

	public static final Codec<SpriteKey> CODEC = Codec.either(MAP_CODEC, ID.CODEC).xmap(either -> either.map(Function.identity(), SpriteKey::block), key -> key.atlas.equals(BLOCKS) ? Either.left(key) : Either.right(key.sprite));

	public static final StreamCodec<ByteBuf, SpriteKey> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC.optional(BLOCKS), SpriteKey::atlas,
		ID.STREAM_CODEC, SpriteKey::sprite,
		SpriteKey::of
	);

	public static final StreamCodec<ByteBuf, Optional<SpriteKey>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.optional();

	public ResourceLocation dynamic() {
		return ResourceLocation.fromNamespaceAndPath(sprite.getNamespace(), "textures/vidlib/generated/atlas/" + atlas.getNamespace() + "/" + atlas.getPath() + "/" + sprite.getPath() + ".png");
	}

	@Override
	@NotNull
	public String toString() {
		return atlas + ":" + sprite;
	}
}
