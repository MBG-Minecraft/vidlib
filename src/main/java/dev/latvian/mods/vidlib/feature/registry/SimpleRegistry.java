package dev.latvian.mods.vidlib.feature.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.RegistryKeys;
import dev.latvian.mods.klib.util.Cast;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SimpleRegistry<V extends SimpleRegistryEntry> {
	public static final Map<Identifier, SimpleRegistry<?>> ALL = new Object2ObjectOpenHashMap<>();

	public static <V extends SimpleRegistryEntry> SimpleRegistry<V> create(RegistryKeys<V> registryKeys, Consumer<CustomRegistryTypeCollector<V>> collector) {
		return new SimpleRegistry<>(registryKeys, collector);
	}

	private final RegistryKeys<V> registryKeys;
	private Consumer<CustomRegistryTypeCollector<V>> collector;
	private final Map<String, CustomRegistryType<V>> typeMap;
	private final Map<V, CustomRegistryType.Unit<V>> unitTypeMap;
	private final Map<String, V> unitValueMap;
	public final Codec<CustomRegistryType<V>> typeCodec;
	private final Codec<V> codec;
	private final StreamCodec<RegistryFriendlyByteBuf, V> streamCodec;

	private SimpleRegistry(RegistryKeys<V> registryKeys, Consumer<CustomRegistryTypeCollector<V>> collector) {
		this.registryKeys = registryKeys;
		this.collector = collector;
		this.typeMap = new Object2ObjectOpenHashMap<>();
		this.unitTypeMap = new IdentityHashMap<>();
		this.unitValueMap = new LinkedHashMap<>();

		this.typeCodec = KLibCodecs.map(typeMap, Codec.STRING, CustomRegistryType::id);

		Codec<V> unitCodec = Codec.STRING.flatXmap(s -> {
			var value = typeMap.get(s);
			return value instanceof CustomRegistryType.Unit<V> unit ? DataResult.success(unit.instance()) : DataResult.error(() -> "Value " + s + " not found");
		}, o -> {
			var unit = unitTypeMap.get(o);
			return unit != null ? DataResult.success(unit.id()) : DataResult.error(() -> "Key " + o + " not found");
		});

		Codec<V> dispatchCodec = typeCodec.dispatch("type", v -> Cast.to(v.type()), CustomRegistryType::codec);

		this.codec = KLibCodecs.or(unitCodec, dispatchCodec);

		this.streamCodec = new StreamCodec<>() {
			@Override
			public V decode(RegistryFriendlyByteBuf buf) {
				return streamDecode(buf);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buf, V value) {
				streamEncode(buf, value);
			}
		};
	}

	public void build() {
		if (collector == null) {
			throw new IllegalStateException("Registry " + registryKeys.root().identifier() + " already built");
		}

		collector.accept(this::register);
		collector = null;
		ALL.put(registryId, this);
	}

	public void register(CustomRegistryType<? extends V> type) {
		typeMap.put(type.id(), Cast.to(type));

		if (type instanceof CustomRegistryType.Unit unit) {
			V unitValue = Cast.to(unit.instance());
			unitTypeMap.put(unitValue, unit);
			unitValueMap.put(unit.id(), unitValue);
		}
	}

	@Nullable
	public CustomRegistryType.Unit<V> getType(V value) {
		return unitTypeMap.get(value);
	}

	public Map<String, V> unitValueMap() {
		return unitValueMap;
	}

	public Codec<V> codec() {
		return codec;
	}

	public StreamCodec<RegistryFriendlyByteBuf, V> streamCodec() {
		return streamCodec;
	}

	public V streamDecode(RegistryFriendlyByteBuf buf) {
		var typeId = buf.readUtf();
		var type = typeMap.get(typeId);

		if (type == null) {
			throw new NullPointerException("Type " + registryId + "/" + typeId + " not found");
		}

		try {
			return type.streamCodec().decode(buf);
		} catch (Throwable ex) {
			throw new IllegalStateException("Failed to decode " + registryId + "/" + typeId, ex);
		}
	}

	public void streamEncode(RegistryFriendlyByteBuf buf, V value) {
		var type = value.type();
		buf.writeUtf(type.id());

		try {
			type.streamCodec().encode(buf, Cast.to(value));
		} catch (Throwable ex) {
			throw new IllegalStateException("Failed to encode " + registryId + "/" + type.id(), ex);
		}
	}

	public ArgumentType<V> argument(CommandBuildContext ctx) {
		return;
	}
}
