package dev.latvian.mods.vidlib.feature.item;

import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.util.IOUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record VisualItemKey(Holder<Item> item, DataComponentPatch visualPatch, UUID uuid) {
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

	public static UUID componentUUID(DataComponentPatch visualPatch, RegistryAccess registryAccess) {
		if (!visualPatch.isEmpty()) {
			try {
				var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
				DataComponentPatch.STREAM_CODEC.encode(buf, visualPatch);
				var md = MessageDigest.getInstance("MD5");
				md.update(IOUtils.toByteArray(buf, true));
				return UUID.nameUUIDFromBytes(md.digest());
			} catch (Exception ignore) {
			}
		}

		return Util.NIL_UUID;
	}

	public static final VisualItemKey AIR = new VisualItemKey(Items.AIR.builtInRegistryHolder(), DataComponentPatch.EMPTY, Util.NIL_UUID);

	public static VisualItemKey of(@Nullable ItemStack stack, RegistryAccess registryAccess) {
		if (stack == null || stack.isEmpty()) {
			return AIR;
		}

		var holder = stack.getItemHolder();
		var key = holder.getKey();
		var patchCache = CACHE.computeIfAbsent(key, k -> new Object2ObjectOpenHashMap<>());

		var visualPatch = visual(stack.getComponentsPatch());
		var itemKey = patchCache.get(visualPatch);

		if (itemKey == null) {
			itemKey = new VisualItemKey(holder, visualPatch, componentUUID(visualPatch, registryAccess));
			patchCache.put(visualPatch, itemKey);
		}

		return itemKey;
	}

	public String toString() {
		var key = item.getKey();
		var str = key.location().getNamespace() + "_" + key.location().getPath().replace('/', '_').replace('.', '_');

		if (!uuid.equals(Util.NIL_UUID)) {
			str += "_" + UndashedUuid.toString(uuid);
		}

		return str;
	}

	public ItemStack toItemStack() {
		return this == AIR ? ItemStack.EMPTY : new ItemStack(item, 1, visualPatch);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof VisualItemKey k && item.is(k.item) && visualPatch.equals(k.visualPatch);
	}
}
