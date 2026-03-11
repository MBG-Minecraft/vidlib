package dev.latvian.mods.vidlib.feature.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Cast;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public record VisualItemKey(Holder<Item> item, DataComponentPatch visualPatch) {
	public static final Map<ResourceKey<Item>, Map<DataComponentPatch, VisualItemKey>> CACHE = new Reference2ObjectOpenHashMap<>();

	public static final Set<DataComponentType<?>> VISUAL_KEYS = new ReferenceOpenHashSet<>(Set.of(
		DataComponents.ITEM_MODEL,
		DataComponents.CUSTOM_MODEL_DATA,
		DataComponents.DYED_COLOR,
		DataComponents.MAP_COLOR,
		DataComponents.POTION_CONTENTS,
		DataComponents.TRIM,
		DataComponents.FIREWORK_EXPLOSION,
		DataComponents.BANNER_PATTERNS,
		DataComponents.BASE_COLOR
	));

	public static DataComponentPatch visual(DataComponentPatch patch) {
		DataComponentPatch.Builder newPatch = null;

		for (var entry : patch.entrySet()) {
			if (VISUAL_KEYS.contains(entry.getKey())) {
				if (newPatch == null) {
					newPatch = DataComponentPatch.builder();
				}

				if (entry.getValue().isPresent()) {
					newPatch.set(entry.getKey(), Cast.to(entry.getValue().get()));
				} else {
					newPatch.remove(entry.getKey());
				}
			}
		}

		return newPatch == null ? DataComponentPatch.EMPTY : newPatch.build();
	}

	public static final VisualItemKey AIR = new VisualItemKey(Items.AIR.builtInRegistryHolder(), DataComponentPatch.EMPTY);

	public static VisualItemKey create(Holder<Item> holder, DataComponentPatch visualPatch) {
		if (holder.value() == Items.AIR) {
			return AIR;
		}

		var key = holder.getKey();
		var patchCache = CACHE.computeIfAbsent(key, k -> new Object2ObjectOpenHashMap<>());
		var itemKey = patchCache.get(visualPatch);

		if (itemKey == null) {
			itemKey = new VisualItemKey(holder, visualPatch);
			patchCache.put(visualPatch, itemKey);
		}

		return itemKey;
	}

	public static VisualItemKey of(@Nullable ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return AIR;
		}

		return create(stack.getItemHolder(), visual(stack.getComponentsPatch()));
	}

	public static final Codec<VisualItemKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(VisualItemKey::item),
		DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(VisualItemKey::visualPatch)
	).apply(instance, VisualItemKey::create));

	public String toString() {
		var key = item.getKey();
		return key.location().getNamespace() + "_" + key.location().getPath().replace('/', '_').replace('.', '_');
	}

	public ItemStack toItemStack() {
		return this == AIR ? ItemStack.EMPTY : new ItemStack(item, 1, visualPatch);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof VisualItemKey k && item.is(k.item) && visualPatch.equals(k.visualPatch);
	}
}
