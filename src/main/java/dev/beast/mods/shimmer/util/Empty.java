package dev.beast.mods.shimmer.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface Empty {
	ResourceLocation ID = ResourceLocation.withDefaultNamespace("empty");
	ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
	Entity[] ENTITY_ARRAY = new Entity[0];
	EmptyCompoundTag COMPOUND_TAG = new EmptyCompoundTag();
	Component COMPONENT = Component.empty();
}
