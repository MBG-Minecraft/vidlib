package dev.beast.mods.shimmer.util.registry;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.Side;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class RegistryReference<K, V> implements Supplier<V> {
	private static final List<Holder<?, ?>> DATA_PACK_HOLDERS = new ArrayList<>();

	public static <K, V> Holder<K, V> createServerHolder() {
		var holder = new Holder<K, V>(Side.SERVER);
		DATA_PACK_HOLDERS.add(holder);
		return holder;
	}

	public static <K, V> Holder<K, V> createClientHolder() {
		return new Holder<>(Side.CLIENT);
	}

	public static <V> IdHolder<V> createServerIdHolder(String id, boolean internal) {
		var holder = new IdHolder<V>(id, internal, Side.SERVER);
		DATA_PACK_HOLDERS.add(holder);
		return holder;
	}

	public static <V> IdHolder<V> createClientIdHolder(String id, boolean internal) {
		return new IdHolder<>(id, internal, Side.CLIENT);
	}

	public static void releaseServerHolders() {
		for (var holder : DATA_PACK_HOLDERS) {
			holder.release();
		}
	}

	public static class Holder<K, V> implements Iterable<V> {
		private final Side side;
		private final Map<K, RegistryReference<K, V>> refMap;
		protected Map<K, V> map;
		protected Map<V, K> reverseMap;

		private Holder(Side side) {
			this.side = side;
			this.refMap = new HashMap<>();
			this.map = Map.of();
			this.reverseMap = Map.of();
		}

		public Side getSide() {
			return side;
		}

		@Override
		@NotNull
		public Iterator<V> iterator() {
			return map.values().iterator();
		}

		public Map<K, V> getMap() {
			return map;
		}

		public RegistryReference<K, V> reference(K id) {
			var ref = refMap.get(id);

			if (ref == null) {
				ref = new RegistryReference<>(id);
				ref.value = map.get(id);
				refMap.put(id, ref);
			}

			return ref;
		}

		@Nullable
		public V get(K id) {
			return map.get(id);
		}

		public K getId(V value) {
			return reverseMap.get(value);
		}

		public void update(Map<K, V> values) {
			map = values;

			for (var ref : refMap.values()) {
				ref.value = values.get(ref.id);
			}

			if (values.isEmpty()) {
				reverseMap = Map.of();
			} else {
				reverseMap = new Reference2ObjectOpenHashMap<>(map.size());

				for (var entry : map.entrySet()) {
					reverseMap.put(entry.getValue(), entry.getKey());
				}
			}
		}

		public void release() {
			update(Map.of());
		}
	}

	public static class IdHolder<V> extends Holder<ResourceLocation, V> implements Supplier<Iterable<ResourceLocation>> {
		public final ResourceLocation id;
		public final Codec<ResourceLocation> keyCodec;
		public final StreamCodec<ByteBuf, ResourceLocation> keyStreamCodec;
		public final SuggestionProvider<CommandSourceStack> suggestionProvider;

		private IdHolder(String _id, boolean internal, Side side) {
			super(side);
			this.id = Shimmer.id(_id);
			this.keyCodec = internal ? ShimmerCodecs.SHIMMER_ID : ShimmerCodecs.VIDEO_ID;
			this.keyStreamCodec = internal ? ShimmerStreamCodecs.SHIMMER_ID : ShimmerStreamCodecs.VIDEO_ID;
			this.suggestionProvider = internal ? ShimmerResourceLocationArgument.registerSuggestionProvider(id, this) : VideoResourceLocationArgument.registerSuggestionProvider(id, this);
		}

		@Override
		public Iterable<ResourceLocation> get() {
			return map.keySet();
		}

		public StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodecOrDirect(KnownCodec<V> knownCodec, StreamCodec<? super RegistryFriendlyByteBuf, V> directStreamCodec) {
			return ByteBufCodecs.either(knownCodec.streamCodec(), directStreamCodec).map(either -> either.map(Function.identity(), Function.identity()), v -> getId(v) != null ? Either.left(v) : Either.right(v));
		}

		@Override
		public String toString() {
			return id.toString();
		}
	}

	private final K id;
	private V value;

	private RegistryReference(K id) {
		this.id = id;
	}

	public K id() {
		return id;
	}

	@Override
	public V get() {
		if (value == null) {
			throw new NullPointerException("Value for " + id + " is null");
		}

		return value;
	}

	public Optional<V> optional() {
		return Optional.ofNullable(value);
	}

	public boolean isSet() {
		return value != null;
	}
}
