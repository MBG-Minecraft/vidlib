package dev.latvian.mods.vidlib.feature.entity.progress;

import net.minecraft.core.ClientAsset;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public record ProgressBarTextures(ClientAsset background, ClientAsset bar) {
	public ProgressBarTextures(Identifier id) {
		this(new ClientAsset.ResourceTexture(id.withPath(p -> "progress_bar/" + p + "/background")), new ClientAsset.ResourceTexture(id.withPath(p -> "progress_bar/" + p + "/bar")));
	}

	public ProgressBarTextures(Holder<EntityType<?>> entityType) {
		this(entityType.key().identifier());
	}
}
