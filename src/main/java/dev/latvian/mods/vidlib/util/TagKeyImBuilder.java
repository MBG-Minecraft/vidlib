package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public class TagKeyImBuilder<T> implements ImBuilder<TagKey<T>> {
	public final ResourceKey<? extends Registry<T>> registry;
	public final TagKey<T>[] tag = new TagKey[1];
	private Registry<T> cachedRegistry;
	private TagKey<T>[] cachedTags;

	public TagKeyImBuilder(ResourceKey<? extends Registry<T>> registry) {
		this.registry = registry;
	}

	@Override
	public void set(TagKey<T> value) {
		tag[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var registryAccess = Minecraft.getInstance().player.connection.registryAccess();
		var reg = registryAccess.lookup(registry).orElse(null);

		if (reg == null) {
			return ImUpdate.NONE;
		}

		if (cachedRegistry != reg) {
			cachedRegistry = reg;
			cachedTags = reg.listTags().map(HolderSet.Named::key).toArray(TagKey[]::new);
		}

		return graphics.combo("###tag", "Select Tag...", tag, cachedTags, t -> t.location().toString(), 0);
	}

	@Override
	public boolean isValid() {
		return tag[0] != null;
	}

	@Override
	public TagKey<T> build() {
		return tag[0];
	}
}
