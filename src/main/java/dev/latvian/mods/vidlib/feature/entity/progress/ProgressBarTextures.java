package dev.latvian.mods.vidlib.feature.entity.progress;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public record ProgressBarTextures(Identifier background, Identifier bar) {
	public ProgressBarTextures(Identifier id) {
		this(id.withPath(p -> "textures/progress_bar/" + p + "/background.png"), id.withPath(p -> "textures/progress_bar/" + p + "/bar.png"));
	}

	public ProgressBarTextures(Holder<EntityType<?>> entityType) {
		this(entityType.getKey().identifier());
	}
}
