package dev.latvian.mods.vidlib.feature.maptextureoverride;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.visual.SpriteKey;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MapTextureOverrides {
	public static final Codec<MapTextureOverrides> CODEC = MapTextureOverride.CODEC.listOf().xmap(list -> {
		var obj = new MapTextureOverrides();
		obj.list.addAll(list);
		return obj;
	}, obj -> List.copyOf(obj.list));

	public static final StreamCodec<ByteBuf, MapTextureOverrides> STREAM_CODEC = KLibStreamCodecs.listOf(MapTextureOverride.STREAM_CODEC).map(o -> {
		var obj = new MapTextureOverrides();
		obj.list.addAll(o);
		return obj;
	}, obj -> List.copyOf(obj.list));

	public static final DataType<MapTextureOverrides> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, MapTextureOverrides.class);
	public static final MapTextureOverrides EMPTY = new MapTextureOverrides();

	public final List<MapTextureOverride> list = new ArrayList<>();
	private Int2ObjectMap<SpriteKey> map;

	public void update() {
		map = null;
	}

	@Nullable
	public SpriteKey get(int id) {
		if (map == null) {
			map = new Int2ObjectOpenHashMap<>(list.size());

			for (var entry : list) {
				map.put(entry.mapId(), entry.sprite());
			}
		}

		return map.get(id);
	}

	public void join(MapTextureOverrides other) {
		list.addAll(other.list);
	}
}
