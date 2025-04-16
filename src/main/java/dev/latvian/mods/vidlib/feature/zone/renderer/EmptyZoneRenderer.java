package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;

public class EmptyZoneRenderer implements ZoneRenderer<ZoneShape> {
	public static final EmptyZoneRenderer INSTANCE = new EmptyZoneRenderer();

	@Override
	public void render(ZoneShape shape, Context ctx) {
	}
}
