package dev.beast.mods.shimmer.feature.skybox;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.JsonCodecReloadListener;
import dev.beast.mods.shimmer.util.registry.RegistryReference;
import dev.beast.mods.shimmer.util.registry.ShimmerResourceLocationArgument;
import io.netty.buffer.ByteBuf;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public record SkyboxData(
	ResourceLocation id,
	Optional<ResourceLocation> texture,
	float rotation,
	float rotating,
	Color tint,
	boolean celestials
) {
	public static final Codec<SkyboxData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ShimmerCodecs.SHIMMER_ID.fieldOf("id").forGetter(SkyboxData::id),
		ShimmerCodecs.SHIMMER_ID.optionalFieldOf("texture").forGetter(SkyboxData::texture),
		Codec.FLOAT.optionalFieldOf("rotation", 0F).forGetter(SkyboxData::rotation),
		Codec.FLOAT.optionalFieldOf("rotating", 0F).forGetter(SkyboxData::rotating),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(SkyboxData::tint),
		Codec.BOOL.optionalFieldOf("celestials", false).forGetter(SkyboxData::celestials)
	).apply(instance, SkyboxData::new));

	public static final StreamCodec<ByteBuf, SkyboxData> STREAM_CODEC = CompositeStreamCodec.of(
		ShimmerStreamCodecs.SHIMMER_ID, SkyboxData::id,
		ShimmerStreamCodecs.SHIMMER_ID.optional(), SkyboxData::texture,
		ByteBufCodecs.FLOAT, SkyboxData::rotation,
		ByteBufCodecs.FLOAT, SkyboxData::rotating,
		Color.STREAM_CODEC, SkyboxData::tint,
		ByteBufCodecs.BOOL, SkyboxData::celestials,
		SkyboxData::new
	);

	public static final RegistryReference.Holder<ResourceLocation, SkyboxData> SERVER = RegistryReference.createServerHolder();
	public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = ShimmerResourceLocationArgument.registerSuggestionProvider(Shimmer.id("skybox"), () -> SERVER.getMap().keySet());

	public static class Loader extends JsonCodecReloadListener<SkyboxData> {
		public Loader() {
			super("shimmer/skybox", CODEC, true);
		}

		@Override
		protected void apply(Map<ResourceLocation, SkyboxData> from) {
			SERVER.update(Map.copyOf(from));
		}
	}
}
