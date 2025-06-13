package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ItemParticleOptions(int lifespan, ItemStack item, float gravity, float scale) implements ParticleOptions {
	public static final MapCodec<ItemParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KLibCodecs.TICKS.optionalFieldOf("ttl", 40).forGetter(ItemParticleOptions::lifespan),
		ItemStack.CODEC.fieldOf("item").forGetter(ItemParticleOptions::item),
		Codec.FLOAT.optionalFieldOf("gravity", 1F).forGetter(ItemParticleOptions::gravity),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(ItemParticleOptions::scale)
	).apply(instance, ItemParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ItemParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, ItemParticleOptions::lifespan,
		ItemStack.STREAM_CODEC, ItemParticleOptions::item,
		ByteBufCodecs.FLOAT.optional(1F), ItemParticleOptions::gravity,
		ByteBufCodecs.FLOAT.optional(1F), ItemParticleOptions::scale,
		ItemParticleOptions::new
	);

	public ItemParticleOptions(int lifespan, ItemStack item) {
		this(lifespan, item, 1F, 1F);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.ITEM.get();
	}
}
