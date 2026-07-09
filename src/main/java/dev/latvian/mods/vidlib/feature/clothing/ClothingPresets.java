package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import io.netty.buffer.ByteBuf;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoInit
public class ClothingPresets {
	public static boolean ready = false;
	public static final ClothingPresets EMPTY = new ClothingPresets(Map.of());
	public static ClothingPresets INSTANCE = EMPTY;

	public static final ResourceKey<? extends Registry<ClothingSet>> ROOT_ID = ResourceKey.createRegistryKey(ID.vidlib("clothing_preset"));

	public static ResourceKey<ClothingSet> createId(Identifier id) {
		return ResourceKey.create(ROOT_ID, id);
	}

	public static final Codec<ResourceKey<ClothingSet>> KEY_CODEC = ID.CODEC.xmap(ClothingPresets::createId, ResourceKey::identifier);
	public static final StreamCodec<ByteBuf, ResourceKey<ClothingSet>> KEY_STREAM_CODEC = ID.STREAM_CODEC.map(ClothingPresets::createId, ResourceKey::identifier);

	public static final List<Identifier> IDS = new ArrayList<>();
	public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = ID.registerSuggestionProvider(ID.vidlib("clothing_preset"), () -> IDS);

	public final Map<ResourceKey<ClothingSet>, ClothingSet> map;
	public final Map<ClothingSet, ResourceKey<ClothingSet>> reverseMap;
	public final List<ResourceKey<ClothingSet>> sortedKeys;

	ClothingPresets(Map<ResourceKey<ClothingSet>, ClothingSet> map) {
		this.map = Map.copyOf(map);

		var reverseMap = new HashMap<ClothingSet, ResourceKey<ClothingSet>>(map.size());

		for (var entry : map.entrySet()) {
			reverseMap.put(entry.getValue(), entry.getKey());
		}

		this.reverseMap = Map.copyOf(reverseMap);

		var sortedKeys = new ArrayList<>(map.keySet());
		sortedKeys.sort((o1, o2) -> o1.identifier().compareNamespaced(o2.identifier()));
		this.sortedKeys = List.copyOf(sortedKeys);
	}
}
