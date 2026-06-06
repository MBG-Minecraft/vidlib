package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class ClothingSet {
	public static final ClothingSet EMPTY = new ClothingSet(List.of());
	private static final ResourceLocation EMPTY_TEXTURE_ASSET = VidLib.id("empty");

	public static ClothingSet of(List<ClothingPart> list) {
		return list.isEmpty() ? EMPTY : new ClothingSet(list);
	}

	public static final Codec<ClothingSet> LIST_CODEC = ClothingPart.CODEC.listOf().xmap(ClothingSet::of, ClothingSet::parts);

	public static final Codec<ClothingSet> MAP_CODEC = Codec.unboundedMap(ID.CODEC, VLCodecs.TRANSPARENT_OR_GRADIENT_CODEC).xmap(
		map -> of(map.entrySet().stream().map(ClothingPart::new).toList()),
		set -> {
			var map = new Object2ObjectLinkedOpenHashMap<ResourceLocation, Gradient>();

			for (var part : set.parts) {
				map.put(part.texture(), part.colors());
			}

			return map;
		}
	);

	public static final Codec<ClothingSet> CODEC = KLibCodecs.or(MAP_CODEC, LIST_CODEC);

	public static final StreamCodec<ByteBuf, ClothingSet> STREAM_CODEC = KLibStreamCodecs.listOf(ClothingPart.STREAM_CODEC).map(ClothingSet::of, ClothingSet::parts);
	public static final DataType<ClothingSet> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, ClothingSet.class);
	public static final CommandDataType<ClothingSet> COMMAND = CommandDataType.of(DATA_TYPE);

	public static ClothingSet join(ClothingSet a, ClothingSet b) {
		if (a.parts.isEmpty()) {
			return b;
		} else if (b.parts.isEmpty()) {
			return a;
		} else {
			var list = new ArrayList<ClothingPart>(a.parts.size() + b.parts.size());
			list.addAll(a.parts);
			list.addAll(b.parts);
			return of(list);
		}
	}

	public final List<ClothingPart> parts;
	private ResourceLocation uniqueId;

	private ClothingSet(List<ClothingPart> parts) {
		this.parts = List.copyOf(parts);
	}

	public List<ClothingPart> parts() {
		return parts;
	}

	public int hashCode() {
		return parts.hashCode();
	}

	public boolean equals(Object o) {
		return o == this || o instanceof ClothingSet set && parts.equals(set.parts);
	}

	public ResourceLocation getUniqueId() {
		if (uniqueId == null) {
			if (!parts.isEmpty()) {
				var id = ClothingPresets.INSTANCE.reverseMap.get(this);

				if (id != null) {
					uniqueId = id.location().withPath("preset/" + id.location().getPath());
				} else {
					try (var out = new ByteArrayOutputStream(); var dataOut = new DataOutputStream(out)) {
						var tag = CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();

						if (tag instanceof CompoundTag c) {
							NbtIo.write(c, dataOut);
						} else {
							var compound = new CompoundTag();
							compound.put("$", tag);
							NbtIo.write(compound, dataOut);
						}

						var hash = StringUtils.toHex(MessageDigest.getInstance("SHA-256").digest(out.toByteArray()));
						uniqueId = VidLib.id("custom/" + hash);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}

		if (uniqueId == null) {
			uniqueId = EMPTY_TEXTURE_ASSET;
		}

		return uniqueId;
	}
}
