package dev.beast.mods.shimmer.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.CompletableFuture;

public interface Empty {
	Object[] OBJECT_ARRAY = new Object[0];
	String[] STRING_ARRAY = new String[0];
	ResourceLocation ID = ResourceLocation.withDefaultNamespace("empty");
	ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
	Entity[] ENTITY_ARRAY = new Entity[0];
	EmptyCompoundTag COMPOUND_TAG = new EmptyCompoundTag();
	Component COMPONENT = Component.empty();
	CompletableFuture<?>[] COMPLETABLE_FUTURES = new CompletableFuture[0];
	GameProfile PROFILE = new GameProfile(Util.NIL_UUID, "");

	static boolean isEmpty(Component component) {
		return component == null || component == COMPONENT || component.getString().isEmpty();
	}
}
