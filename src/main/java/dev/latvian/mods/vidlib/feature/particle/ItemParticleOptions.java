package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ItemParticleOptions(ItemStack item, float gravity, float scale, int ttl) implements ParticleOptions {
	public static final MapCodec<ItemParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ItemStack.CODEC.fieldOf("item").forGetter(ItemParticleOptions::item),
		Codec.FLOAT.optionalFieldOf("gravity", 1F).forGetter(ItemParticleOptions::gravity),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(ItemParticleOptions::scale),
		Codec.INT.optionalFieldOf("ttl", 40).forGetter(ItemParticleOptions::ttl)
	).apply(instance, ItemParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ItemParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ItemStack.STREAM_CODEC, ItemParticleOptions::item,
		ByteBufCodecs.FLOAT.optional(1F), ItemParticleOptions::gravity,
		ByteBufCodecs.FLOAT.optional(1F), ItemParticleOptions::scale,
		ByteBufCodecs.VAR_INT, ItemParticleOptions::ttl,
		ItemParticleOptions::new
	);

	public ItemParticleOptions(ItemStack item, int ttl) {
		this(item, 1F, 1F, ttl);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.CUBE.get();
	}
}
