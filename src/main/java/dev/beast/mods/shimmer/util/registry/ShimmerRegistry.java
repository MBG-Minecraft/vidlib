package dev.beast.mods.shimmer.util.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ShimmerRegistry<V> extends BasicShimmerRegistry<ResourceLocation, V> implements Supplier<Iterable<ResourceLocation>>, BiFunction<KnownCodec<V>, CommandBuildContext, ArgumentType<V>> {
	public static <V> ShimmerRegistry<V> createServer(String id, boolean preferInternal) {
		var holder = new ShimmerRegistry<V>(id, preferInternal, Side.SERVER);
		DATA_PACK_HOLDERS.add(holder);
		return holder;
	}

	public static <V> ShimmerRegistry<V> createClient(String id, boolean preferInternal) {
		return new ShimmerRegistry<>(id, preferInternal, Side.CLIENT);
	}

	public final ResourceLocation id;
	public final boolean preferInternal;
	public final Codec<ResourceLocation> keyCodec;
	public final StreamCodec<ByteBuf, ResourceLocation> keyStreamCodec;
	public final SuggestionProvider<CommandSourceStack> suggestionProvider;

	private ShimmerRegistry(String _id, boolean preferInternal, Side side) {
		super(side);
		this.id = Shimmer.id(_id);
		this.preferInternal = preferInternal;
		this.keyCodec = preferInternal ? ShimmerCodecs.SHIMMER_ID : ShimmerCodecs.VIDEO_ID;
		this.keyStreamCodec = preferInternal ? ShimmerStreamCodecs.SHIMMER_ID : ShimmerStreamCodecs.VIDEO_ID;
		this.suggestionProvider = preferInternal ? ShimmerResourceLocationArgument.registerSuggestionProvider(id, this) : VideoResourceLocationArgument.registerSuggestionProvider(id, this);
	}

	@Override
	public Iterable<ResourceLocation> get() {
		return map.keySet();
	}

	public StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodecOrDirect(KnownCodec<V> knownCodec, StreamCodec<? super RegistryFriendlyByteBuf, V> directStreamCodec) {
		return ByteBufCodecs.either(knownCodec.streamCodec(), directStreamCodec).map(either -> either.map(Function.identity(), Function.identity()), v -> getId(v) != null ? Either.left(v) : Either.right(v));
	}

	@Override
	public RegistryRef<V> ref(ResourceLocation id) {
		var ref = refMap.get(id);

		if (ref == null) {
			ref = new RegistryRef<>(id);
			ref.value = map.get(id);
			refMap.put(id, ref);
		}

		return (RegistryRef<V>) ref;
	}

	@Override
	public String toString() {
		return id.toString();
	}

	@Override
	public ArgumentType<V> apply(KnownCodec<V> knownCodec, CommandBuildContext commandBuildContext) {
		return new RefHolderArgument<>(this, knownCodec);
	}
}
