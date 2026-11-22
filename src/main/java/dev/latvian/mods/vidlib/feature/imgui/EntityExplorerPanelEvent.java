package dev.latvian.mods.vidlib.feature.imgui;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityEvent;

public class EntityExplorerPanelEvent extends EntityEvent {
	private final ImGraphics graphics;
	private final float delta;

	public EntityExplorerPanelEvent(Entity entity, ImGraphics graphics, float delta) {
		super(entity);
		this.graphics = graphics;
		this.delta = delta;
	}

	public ImGraphics getGraphics() {
		return graphics;
	}

	public float getDelta() {
		return delta;
	}
}
