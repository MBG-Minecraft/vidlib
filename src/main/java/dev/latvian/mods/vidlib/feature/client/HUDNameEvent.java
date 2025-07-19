package dev.latvian.mods.vidlib.feature.client;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

import java.util.List;

public class HUDNameEvent extends Event {
	private final Entity entity;
	private final Font font;
	private final List<FormattedCharSequence> lines;

	public HUDNameEvent(Entity entity, Font font, List<FormattedCharSequence> lines) {
		this.entity = entity;
		this.font = font;
		this.lines = lines;
	}

	public Entity getEntity() {
		return entity;
	}

	public Font getFont() {
		return font;
	}

	public List<FormattedCharSequence> getLines() {
		return lines;
	}

	public void add(FormattedText text) {
		lines.addAll(font.split(text, 1000));
	}

	public void add(String text) {
		lines.addAll(font.split(FormattedText.of(text), 1000));
	}
}
