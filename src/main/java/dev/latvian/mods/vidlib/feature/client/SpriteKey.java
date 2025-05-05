package dev.latvian.mods.vidlib.feature.client;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.ID;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record SpriteKey(ResourceLocation atlas, ResourceLocation sprite) {
	public static final ResourceLocation BLOCKS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
	public static final ResourceLocation PARTICLES = ResourceLocation.withDefaultNamespace("textures/atlas/particles.png");

	public static final Codec<SpriteKey> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.optionalFieldOf("atlas", BLOCKS).forGetter(SpriteKey::atlas),
		ID.CODEC.fieldOf("sprite").forGetter(SpriteKey::sprite)
	).apply(instance, SpriteKey::new));

	public static final Codec<SpriteKey> CODEC = Codec.either(MAP_CODEC, ID.CODEC).xmap(either -> either.map(Function.identity(), SpriteKey::block), key -> key.atlas.equals(BLOCKS) ? Either.left(key) : Either.right(key.sprite));

	public static final StreamCodec<ByteBuf, SpriteKey> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC.optional(BLOCKS), SpriteKey::atlas,
		ID.STREAM_CODEC, SpriteKey::sprite,
		SpriteKey::new
	);

	public static SpriteKey block(ResourceLocation sprite) {
		return new SpriteKey(BLOCKS, sprite);
	}

	public static SpriteKey particle(ResourceLocation sprite) {
		return new SpriteKey(PARTICLES, sprite);
	}

	public ResourceLocation dynamic() {
		return ResourceLocation.fromNamespaceAndPath(sprite.getNamespace(), "textures/vidlib/generated/atlas/" + atlas.getNamespace() + "/" + atlas.getPath() + "/" + sprite.getPath() + ".png");
	}
}
