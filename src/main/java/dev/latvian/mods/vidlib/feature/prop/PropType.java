package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Predicate;

@AutoInit
public record PropType<P extends Prop>(
	ResourceLocation id,
	Factory<? extends P> factory,
	Map<String, PropData<?, ?>> data,
	Int2ObjectMap<PropData<?, ?>> idMap,
	Reference2IntMap<PropData<?, ?>> reverseIdMap
) implements PropDataProvider, Predicate<Prop> {
	@FunctionalInterface
	public interface Factory<P extends Prop> {
		P create(PropContext<?> ctx);
	}

	public static final Lazy<Map<ResourceLocation, PropType<?>>> ALL = Lazy.map(map -> {
		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof PropType<?> propType) {
				map.put(propType.id, propType);
			}
		}
	});

	public static <P extends Prop> PropType<P> create(ResourceLocation id, Factory<? extends P> factory, PropDataProvider data) {
		var map = Map.copyOf(data.data());
		var sortedList = new ArrayList<PropData<?, ?>>();

		for (var d : map.values()) {
			if (d.sync()) {
				sortedList.add(d);
			}
		}

		sortedList.sort((a, b) -> a.key().compareToIgnoreCase(b.key()));
		var reverseIdMap = new Reference2IntOpenHashMap<PropData<?, ?>>();
		reverseIdMap.defaultReturnValue(-1);
		var idMap = new Int2ObjectOpenHashMap<PropData<?, ?>>();

		for (int i = 0; i < sortedList.size(); i++) {
			var p = sortedList.get(i);
			reverseIdMap.put(p, i);
			idMap.put(i, p);
		}

		return new PropType<>(id, factory, map, idMap, reverseIdMap);
	}

	public static final Codec<PropType<?>> CODEC = KLibCodecs.map(ALL, ID.CODEC, PropType::id);
	public static final StreamCodec<ByteBuf, PropType<?>> STREAM_CODEC = KLibStreamCodecs.map(ALL, ID.STREAM_CODEC, PropType::id);
	public static final DataType<PropType<?>> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Cast.to(PropType.class));

	@Override
	public boolean test(Prop prop) {
		return prop.type == this;
	}

	public <O> DataResult<P> load(P prop, DynamicOps<O> ops, O map, boolean full) {
		for (var p : data.values()) {
			var t = ops.get(map, p.key());

			if (t.isSuccess()) {
				var result = p.type().codec().parse(ops, t.getOrThrow());

				if (result.isError()) {
					return result.map(o -> prop);
				}

				p.set(Cast.to(prop), Cast.to(result.getOrThrow()));
			} else if (full && p.save() && p.isRequired()) {
				return DataResult.error(() -> "Missing required data key '" + p.key() + "'");
			}
		}

		return DataResult.success(prop);
	}
}
