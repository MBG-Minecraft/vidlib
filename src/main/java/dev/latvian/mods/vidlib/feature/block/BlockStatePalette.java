package dev.latvian.mods.vidlib.feature.block;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Function;

public record BlockStatePalette(Reference2IntMap<BlockState> map, int totalWeight) {
	public static final Codec<BlockStatePalette> DIRECT_CODEC = Codec.unboundedMap(MCCodecs.BLOCK_STATE, Codec.INT).xmap(m -> new BlockStatePalette(new Reference2IntLinkedOpenHashMap<>(m)), BlockStatePalette::map);
	public static final Codec<BlockStatePalette> CODEC = Codec.either(DIRECT_CODEC, MCCodecs.BLOCK_STATE).xmap(e -> e.map(Function.identity(), BlockStatePalette::new), p -> p.map.size() == 1 ? Either.right(p.map.reference2IntEntrySet().iterator().next().getKey()) : Either.left(p));

	public static final StreamCodec<ByteBuf, BlockStatePalette> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public BlockStatePalette decode(ByteBuf buf) {
			int count = VarInt.read(buf);
			var map = new Reference2IntLinkedOpenHashMap<BlockState>(count);
			int totalWeight = 0;

			for (int i = 0; i < count; i++) {
				var state = MCStreamCodecs.BLOCK_STATE.decode(buf);
				int weight = VarInt.read(buf);
				map.put(state, weight);
				totalWeight += weight;
			}

			return new BlockStatePalette(map, totalWeight);
		}

		@Override
		public void encode(ByteBuf buf, BlockStatePalette value) {
			VarInt.write(buf, value.map.size());

			for (var entry : value.map.reference2IntEntrySet()) {
				MCStreamCodecs.BLOCK_STATE.encode(buf, entry.getKey());
				VarInt.write(buf, entry.getIntValue());
			}
		}
	};

	public static final DataType<BlockStatePalette> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, BlockStatePalette.class);

	public BlockStatePalette(Reference2IntMap<BlockState> map) {
		this(map, map.values().stream().mapToInt(Integer::intValue).sum());
	}

	public BlockStatePalette(BlockState state) {
		this(new Reference2IntLinkedOpenHashMap<>(Map.of(state, 1)), 1);
	}

	public BlockState get(int at) {
		for (var entry : map.reference2IntEntrySet()) {
			at -= entry.getIntValue();

			if (at < 0) {
				return entry.getKey();
			}
		}

		return Blocks.AIR.defaultBlockState();
	}

	public BlockState sample(RandomSource random) {
		return get(random.nextInt(totalWeight));
	}
}
