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
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@AutoInit
public record PropType<P extends Prop>(
	ResourceLocation id,
	Factory<? extends P> factory,
	List<PropDataEntry> data,
	Map<PropData<?, ?>, PropDataEntry> reverseData,
	List<PropPacketEntry> packets,
	Map<PropPacketType<?, ?>, PropPacketEntry> reversePackets,
	String translationKey
) implements PropTypeInfo, Predicate<Prop> {
	@FunctionalInterface
	public interface Factory<P extends Prop> {
		P create(PropContext<?> ctx);
	}

	public record PropDataEntry(int index, PropData<?, ?> data) {
	}

	public record PropPacketEntry(int index, PropPacketType<?, ?> packet) {
	}

	public static final Lazy<Map<ResourceLocation, PropType<?>>> ALL = Lazy.map(map -> {
		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof PropType<?> propType) {
				map.put(propType.id, propType);
			}
		}
	});

	public static <P extends Prop> PropType<P> create(ResourceLocation id, Factory<? extends P> factory, PropTypeInfo... info) {
		var dataMap = new LinkedHashMap<String, PropData<?, ?>>();
		var packetSet = new LinkedHashSet<PropPacketType<?, ?>>();

		for (var data : info) {
			if (data instanceof PropType<?> type) {
				for (var entry : type.data) {
					dataMap.put(entry.data.key(), entry.data);
				}

				for (var entry : type.packets) {
					packetSet.add(entry.packet);
				}
			} else if (data instanceof PropData<?, ?> propData) {
				dataMap.put(propData.key(), propData);
			} else if (data instanceof PropPacketType<?, ?> packetType) {
				packetSet.add(packetType);
			}
		}

		var sortedDataList = dataMap.values().stream().filter(PropData::sync).sorted(PropData.COMPARATOR).toList();
		var sortedPacketList = List.copyOf(packetSet);

		var data = new ArrayList<PropDataEntry>(sortedDataList.size());
		var reverseData = new Reference2ObjectOpenHashMap<PropData<?, ?>, PropDataEntry>(sortedDataList.size());

		var packets = new ArrayList<PropPacketEntry>(sortedPacketList.size());
		var reversePackets = new Reference2ObjectOpenHashMap<PropPacketType<?, ?>, PropPacketEntry>(sortedPacketList.size());

		for (int i = 0; i < sortedDataList.size(); i++) {
			var p = new PropDataEntry(i, sortedDataList.get(i));
			reverseData.put(p.data, p);
			data.add(p);
		}

		for (int i = 0; i < sortedPacketList.size(); i++) {
			var p = new PropPacketEntry(i, sortedPacketList.get(i));
			reversePackets.put(p.packet, p);
			packets.add(p);
		}

		return new PropType<>(id, factory, List.copyOf(data), Collections.unmodifiableMap(reverseData), List.copyOf(packets), Collections.unmodifiableMap(reversePackets), Util.makeDescriptionId("prop", id));
	}

	public static final Codec<PropType<?>> CODEC = KLibCodecs.map(ALL, ID.CODEC, PropType::id);
	public static final StreamCodec<ByteBuf, PropType<?>> STREAM_CODEC = KLibStreamCodecs.map(ALL, ID.STREAM_CODEC, PropType::id);
	public static final DataType<PropType<?>> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Cast.to(PropType.class));

	@Override
	public boolean test(Prop prop) {
		return prop.type == this;
	}

	public <O> DataResult<P> load(P prop, DynamicOps<O> ops, O initialData, boolean validate) {
		for (var entry : data) {
			var p = entry.data();
			var t = ops.get(initialData, p.key());

			if (t.isSuccess()) {
				var result = p.type().codec().parse(ops, t.getOrThrow());

				if (result.isError()) {
					return result.map(o -> prop);
				}

				prop.setData(p, Cast.to(result.getOrThrow()));
			} else if (validate && p.isRequired()) {
				return DataResult.error(() -> "Missing required data key '" + p.key() + "'");
			}
		}

		return DataResult.success(prop);
	}

	@Nullable
	public PropDataEntry getData(int index) {
		return index < 0 || index >= data.size() ? null : data.get(index);
	}

	public int getDataIndex(PropData<?, ?> data) {
		var r = reverseData.get(data);
		return r == null ? -1 : r.index;
	}

	@Nullable
	public PropPacketEntry getPacket(int index) {
		return index < 0 || index >= packets.size() ? null : packets.get(index);
	}

	public int getPacketIndex(PropPacketType<?, ?> packet) {
		var r = reversePackets.get(packet);
		return r == null ? -1 : r.index;
	}
}
