package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.util.MiscUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public interface VLStreamCodec<B, V> {
	default StreamCodec<B, V> vl$self() {
		return (StreamCodec) this;
	}

	default StreamCodec<B, Optional<V>> optional() {
		var self = vl$self();

		return new StreamCodec<>() {
			@Override
			public Optional<V> decode(B buf) {
				return ((ByteBuf) buf).readBoolean() ? Optional.of(self.decode(buf)) : Optional.empty();
			}

			@Override
			public void encode(B buf, Optional<V> value) {
				if (value.isPresent()) {
					((ByteBuf) buf).writeBoolean(true);
					self.encode(buf, value.get());
				} else {
					((ByteBuf) buf).writeBoolean(false);
				}
			}
		};
	}

	default StreamCodec<B, V> optional(@Nullable V defaultValue) {
		var self = vl$self();

		return new StreamCodec<>() {
			@Override
			public V decode(B buf) {
				return ((ByteBuf) buf).readBoolean() ? self.decode(buf) : defaultValue;
			}

			@Override
			public void encode(B buf, V value) {
				if (!Objects.equals(value, defaultValue)) {
					((ByteBuf) buf).writeBoolean(true);
					self.encode(buf, value);
				} else {
					((ByteBuf) buf).writeBoolean(false);
				}
			}
		};
	}

	default StreamCodec<B, V> nullable() {
		return optional(null);
	}

	default StreamCodec<B, List<V>> listOf() {
		var self = vl$self();

		return new StreamCodec<>() {
			@Override
			public List<V> decode(B buffer) {
				int size = VarInt.read((ByteBuf) buffer);

				if (size == 0) {
					return List.of();
				} else if (size == 1) {
					return List.of(self.decode(buffer));
				} else {
					var list = new ArrayList<V>(size);

					for (int i = 0; i < size; i++) {
						list.add(self.decode(buffer));
					}

					return list;
				}
			}

			@Override
			public void encode(B buffer, List<V> value) {
				VarInt.write((ByteBuf) buffer, value.size());

				for (V v : value) {
					self.encode(buffer, v);
				}
			}
		};
	}

	default StreamCodec<B, Set<V>> setOf() {
		var self = vl$self();

		return new StreamCodec<>() {
			@Override
			public Set<V> decode(B buffer) {
				int size = VarInt.read((ByteBuf) buffer);

				if (size == 0) {
					return Set.of();
				} else if (size == 1) {
					return Set.of(self.decode(buffer));
				} else {
					var set = new HashSet<V>(size);

					for (int i = 0; i < size; i++) {
						set.add(self.decode(buffer));
					}

					return set;
				}
			}

			@Override
			public void encode(B buffer, Set<V> value) {
				VarInt.write((ByteBuf) buffer, value.size());

				for (V v : value) {
					self.encode(buffer, v);
				}
			}
		};
	}

	default StreamCodec<B, Set<V>> linkedSet() {
		var self = vl$self();

		return new StreamCodec<>() {
			@Override
			public Set<V> decode(B buffer) {
				int size = VarInt.read((ByteBuf) buffer);

				if (size == 0) {
					return Set.of();
				} else if (size == 1) {
					return Set.of(self.decode(buffer));
				} else {
					var set = new LinkedHashSet<V>(size);

					for (int i = 0; i < size; i++) {
						set.add(self.decode(buffer));
					}

					return set;
				}
			}

			@Override
			public void encode(B buffer, Set<V> value) {
				VarInt.write((ByteBuf) buffer, value.size());

				for (V v : value) {
					self.encode(buffer, v);
				}
			}
		};
	}

	default <TB extends ByteBuf, T> StreamCodec<TB, Map<V, T>> unboundedMap(StreamCodec<? super TB, T> valueCodec, boolean ordered, boolean identity) {
		var self = vl$self();

		return new StreamCodec<>() {
			@Override
			public Map<V, T> decode(TB buf) {
				int size = VarInt.read(buf);

				if (size == 0) {
					return Map.of();
				} else if (size == 1) {
					return Map.of(self.decode((B) buf), valueCodec.decode(buf));
				} else {
					var map = MiscUtils.<V, T>createMap(size, ordered, identity);

					for (int i = 0; i < size; i++) {
						map.put(self.decode((B) buf), valueCodec.decode(buf));
					}

					return map;
				}
			}

			@Override
			public void encode(TB buf, Map<V, T> value) {
				VarInt.write(buf, value.size());

				if (!value.isEmpty()) {
					for (var entry : value.entrySet()) {
						self.encode((B) buf, entry.getKey());
						valueCodec.encode(buf, entry.getValue());
					}
				}
			}
		};
	}

	default <TB extends ByteBuf, T> StreamCodec<TB, Map<V, T>> unboundedMap(StreamCodec<? super TB, T> valueCodec) {
		return unboundedMap(valueCodec, false, false);
	}
}
