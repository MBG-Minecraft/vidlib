package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.util.Lazy;
import net.neoforged.neoforge.common.NeoForge;

public enum ImIcons {
	SQUARE('\ueb36'),
	CIRCLE('\uef4a'),
	HEXAGON('\ueb39'),
	DELETE('\ue872'),
	EDIT('\ue3c9'),
	OPEN('\ue2c8'),
	CODE('\ue86f'),
	VISIBLE('\ue8f4'),
	INVISIBLE('\ue8f5'),
	ADD('\ue145'),
	REMOVE('\ue15b'),
	CLOSE('\ue5cd'),
	CHECK('\ue5ca'),
	SEARCH('\ue8b6'),
	MENU('\ue5d2'),
	STAR('\ue838'),
	DOWNLOAD('\uf090'),
	SELECT('\uf74d'),
	ERROR('\ue000'),
	WARNING('\ue002'),
	WRENCH('\ue869'),
	SETTINGS('\ue8b8'),
	RELOAD('\ue86a'),
	CAMERA('\ue04b'),
	APERTURE('\ue3af'),
	SPEED('\ue9e4'),
	TIMELAPSE('\ue422'),
	BRIGHTNESS('\ue518'),
	FREEZE('\ueb3b'),
	LOCATION('\ue55f'),
	BUG('\ue868'),
	HOME('\ue88a'),
	MEMORY('\ue322'),
	UNDO('\ue166'),
	REDO('\ue15a'),
	ARROW_LEFT('\ue5c4'),
	ARROW_RIGHT('\ue5c8'),
	ARROW_UP('\ue5d8'),
	ARROW_DOWN('\ue5db'),
	NAVIGATION('\ue55d'),
	TARGET('\ue55c'),
	KEY('\ue73c'),
	LOCK('\ue897'),
	WORLD('\ue80b'),
	FLAG('\ue153'),
	HEART('\ue87d'),
	HEART_BORDER('\ue87e'),
	FILTER('\uef4f'),
	ACCOUNT('\ue853'),
	DOCUMENT('\ue873'),
	SCHEDULE('\ue8b5'),
	DASHBOARD('\ue871'),
	PAID('\uf041'),
	MONEY('\ue227'),
	FRAMED_CUBE('\ue9fe'),
	LABEL('\ue892'),
	QUESTION('\ueb8b'),
	FULLSCREEN('\ue5d0'),

	;

	public static final ImIcons[] VALUES = values();

	public static final Lazy<char[]> EXTRA_ICONS = Lazy.of(() -> {
		var sb = new StringBuilder();
		NeoForge.EVENT_BUS.post(new ImIconsEvent(sb::append));
		return sb.toString().toCharArray();
	});

	public final char icon;
	private final String iconString;

	ImIcons(char icon) {
		this.icon = icon;
		this.iconString = String.valueOf(icon);
	}

	@Override
	public String toString() {
		return iconString;
	}
}
