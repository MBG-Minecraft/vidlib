package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public class TagKeyImBuilder<T> implements ImBuilder<TagKey<T>> {
	public static <T> ImBuilderType<TagKey<T>> type(ResourceKey<? extends Registry<T>> registry) {
		return () -> new TagKeyImBuilder<>(registry);
	}

	public static final ImBuilderType<TagKey<Block>> BLOCK_TYPE = type(Registries.BLOCK);
	public static final ImBuilderType<TagKey<Item>> ITEM_TYPE = type(Registries.ITEM);
	public static final ImBuilderType<TagKey<Fluid>> FLUID_TYPE = type(Registries.FLUID);
	public static final ImBuilderType<TagKey<EntityType<?>>> ENTITY_TYPE_TYPE = type(Registries.ENTITY_TYPE);

	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final ResourceKey<? extends Registry<T>> registry;
	public final TagKey<T>[] tag = new TagKey[1];
	private Registry<T> cachedRegistry;
	private List<TagKey<T>> cachedTags;

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
			cachedTags = reg.listTags().map(HolderSet.Named::key).toList();
		}

		return graphics.combo("###tag", tag, cachedTags, t -> t.location().toString(), SEARCH);
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
