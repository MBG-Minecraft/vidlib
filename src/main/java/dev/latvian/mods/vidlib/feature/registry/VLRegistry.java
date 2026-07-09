package dev.latvian.mods.vidlib.feature.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.data.ArgumentTypeProvider;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypeCommandInfo;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.RegistryKeys;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Side;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.codec.DataArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class VLRegistry<V> extends GenericVLRegistry<Identifier, V> implements Supplier<Iterable<Identifier>>, ArgumentTypeProvider<V> {
	public static <V> VLRegistry<V> createServer(RegistryKeys<V> registryKeys) {
		var holder = new VLRegistry<>(Side.SERVER, registryKeys);
		DATA_PACK_HOLDERS.add(holder);
		return holder;
	}

	public static <V> VLRegistry<V> createClient(RegistryKeys<V> registryKeys) {
		return new VLRegistry<>(Side.CLIENT, registryKeys);
	}

	public final RegistryKeys<V> registryKeys;
	private final List<Identifier> ids;
	private final List<ResourceKey<V>> keys;
	private DataType<V> dataType;
	private DataType<Ref<V>> refDataType;

	private VLRegistry(Side side, RegistryKeys<V> registryKeys) {
		super(side);
		this.registryKeys = registryKeys;
		this.ids = new ArrayList<>();
		this.keys = new ArrayList<>();
	}

	@Override
	public Iterable<Identifier> get() {
		return map.keySet();
	}

	public DataType<V> orDirect(DataType<V> direct) {
		return DataType.either(dataType(), direct, Function.identity(), Function.identity(), v -> getId(v) != null ? Either.left(v) : Either.right(v));
	}

	@Override
	public synchronized Ref<V> ref(Identifier id) {
		var ref = refMap.get(id);

		if (ref == null) {
			ref = new Ref<>(id);
			ref.value = map.get(id);
			refMap.put(id, ref);
		}

		return (Ref<V>) ref;
	}

	public Ref<V> asRef(V value, Function<V, Identifier> idGetter) {
		var id = idGetter.apply(value);
		return id == null ? null : ref(id);
	}

	@Override
	public String toString() {
		return registryKeys.root().identifier().toString();
	}

	@Override
	public ArgumentType<?> create(DataTypeCommandInfo<V> self, CommandBuildContext ctx) {
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
			}), ID.STREAM_CODEC.map(this::get, this::getId));
		}

		return dataType;
	}

	public DataType<Ref<V>> refDataType() {
		if (refDataType == null) {
			refDataType = DataType.of(ID.CODEC.xmap(this::ref, Ref::id), ID.STREAM_CODEC.map(this::ref, Ref::id), Cast.to(Ref.class));
		}

		return refDataType;
	}

	@Override
	public synchronized void update(Map<Identifier, V> values) {
		super.update(values);
		ids.clear();
		ids.addAll(values.keySet());
		ids.sort(Identifier::compareNamespaced);
		keys.clear();

		for (var id : ids) {
			keys.add(registryKeys.create(id));
		}
	}

	public List<Identifier> getIds() {
		return ids;
	}

	public List<ResourceKey<V>> getKeys() {
		return keys;
	}

	public ArgumentType<V> argument(CommandBuildContext ctx) {
		return null;
	}

	public ArgumentType<ResourceKey<V>> keyArgument(CommandBuildContext ctx) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public V get(CommandContext<CommandSourceStack> ctx, String name) {
		return (V) ctx.getArgument(name, Object.class);
	}

	@SuppressWarnings("unchecked")
	public ResourceKey<V> getKey(CommandContext<CommandSourceStack> ctx, String name) {
		return (ResourceKey<V>) ctx.getArgument(name, ResourceKey.class);
	}
}
