package dev.latvian.mods.vidlib.feature.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.RegisteredDataType;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Side;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.codec.DataArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class VLRegistry<V> extends GenericVLRegistry<ResourceLocation, V> implements Supplier<Iterable<ResourceLocation>>, BiFunction<RegisteredDataType<V>, CommandBuildContext, ArgumentType<V>> {
	public static <V> VLRegistry<V> createServer(String id, Class<V> valueType) {
		var holder = new VLRegistry<>(Side.SERVER, id, valueType);
		DATA_PACK_HOLDERS.add(holder);
		return holder;
	}

	public static <V> VLRegistry<V> createClient(String id, Class<V> valueType) {
		return new VLRegistry<>(Side.CLIENT, id, valueType);
	}

	public final ResourceLocation id;
	public final Class<V> valueType;
	private DataType<V> dataType;
	private DataType<RegistryRef<V>> refDataType;

	private VLRegistry(Side side, String id, Class<V> valueType) {
		super(side);
		this.id = VidLib.id(id);
		this.valueType = valueType;
	}

	@Override
	public Iterable<ResourceLocation> get() {
		return map.keySet();
	}

	public DataType<V> orDirect(DataType<V> direct) {
		return DataType.of(
			Codec.either(dataType().codec(), direct.codec()).xmap(e -> e.map(Function.identity(), Function.identity()), v -> getId(v) != null ? Either.left(v) : Either.right(v)),
			ByteBufCodecs.either(dataType().streamCodec(), direct.streamCodec()).map(e -> e.map(Function.identity(), Function.identity()), v -> getId(v) != null ? Either.left(v) : Either.right(v)),
			valueType
		);
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
	public ArgumentType<V> apply(RegisteredDataType<V> dataType, CommandBuildContext ctx) {
		var ops = ctx.createSerializationContext(NbtOps.INSTANCE);
		var fallback = new DataArgumentType<>(ops, TagParser.create(ops), CommandDataType.of(dataType.type()));
		return new RegistryOrDataArgumentType<>(this, fallback);
	}

	public DataType<V> dataType() {
		if (dataType == null) {
			dataType = DataType.of(ID.CODEC.flatXmap(id -> {
				var value = get(id);
				return value == null ? DataResult.error(() -> "Not found") : DataResult.success(value);
			}, value -> {
				var id = getId(value);
				return id == null ? DataResult.error(() -> "Not found") : DataResult.success(id);
			}), ID.STREAM_CODEC.map(this::get, this::getId), valueType);
		}

		return dataType;
	}

	public DataType<RegistryRef<V>> refDataType() {
		if (refDataType == null) {
			refDataType = DataType.of(ID.CODEC.xmap(this::ref, RegistryRef::id), ID.STREAM_CODEC.map(this::ref, RegistryRef::id), Cast.to(RegistryRef.class));
		}

		return refDataType;
	}
}
