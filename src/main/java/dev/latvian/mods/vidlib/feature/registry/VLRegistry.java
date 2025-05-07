package dev.latvian.mods.vidlib.feature.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import dev.latvian.mods.vidlib.util.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class VLRegistry<V> extends GenericVLRegistry<ResourceLocation, V> implements Supplier<Iterable<ResourceLocation>>, BiFunction<KnownCodec<V>, CommandBuildContext, ArgumentType<V>> {
	public static <V> VLRegistry<V> createServer(String id) {
		var holder = new VLRegistry<V>(id, Side.SERVER);
		DATA_PACK_HOLDERS.add(holder);
		return holder;
	}

	public static <V> VLRegistry<V> createClient(String id) {
		return new VLRegistry<>(id, Side.CLIENT);
	}

	public final ResourceLocation id;
	private Codec<V> valueCodec;
	private Codec<RegistryRef<V>> refCodec;
	private StreamCodec<ByteBuf, V> valueStreamCodec;

	private VLRegistry(String _id, Side side) {
		super(side);
		this.id = VidLib.id(_id);
	}

	@Override
	public Iterable<ResourceLocation> get() {
		return map.keySet();
	}

	public StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodecOrDirect(KnownCodec<V> knownCodec, StreamCodec<? super RegistryFriendlyByteBuf, V> directStreamCodec) {
		return ByteBufCodecs.either(knownCodec.streamCodec(), directStreamCodec).map(either -> either.map(Function.identity(), Function.identity()), v -> getId(v) != null ? Either.left(v) : Either.right(v));
	}

	@Override
	public synchronized RegistryRef<V> ref(ResourceLocation id) {
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

	public Codec<V> valueCodec() {
		if (valueCodec == null) {
			valueCodec = ID.CODEC.flatXmap(id -> {
				var value = get(id);
				return value == null ? DataResult.error(() -> "Not found") : DataResult.success(value);
			}, value -> {
				var id = getId(value);
				return id == null ? DataResult.error(() -> "Not found") : DataResult.success(id);
			});
		}

		return valueCodec;
	}

	public Codec<RegistryRef<V>> refCodec() {
		if (refCodec == null) {
			refCodec = ID.CODEC.flatXmap(id -> {
				var value = ref(id);
				return value == null ? DataResult.error(() -> "Not found") : DataResult.success(value);
			}, value -> DataResult.success(value.id()));
		}

		return refCodec;
	}

	public StreamCodec<ByteBuf, V> valueStreamCodec() {
		if (valueStreamCodec == null) {
			valueStreamCodec = ID.STREAM_CODEC.map(this::get, this::getId);
		}

		return valueStreamCodec;
	}
}
