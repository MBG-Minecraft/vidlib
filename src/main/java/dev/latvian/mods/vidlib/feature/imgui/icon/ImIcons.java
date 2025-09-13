package dev.latvian.mods.vidlib.feature.imgui.icon;

import dev.latvian.mods.klib.util.Lazy;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public enum ImIcons implements ImIcon {
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
	FIRE('\uef55'),
	ROCKET('\ueba5'),
	ROCKET_LAUNCH('\ueb9b'),
	EXPLOSION('\uf685'),
	BOMB('\uf568'),
	VOLCANO('\uebda'),
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
	ROTATE('\ue577'),
	RECYCLE('\ue760'),
	LOGIN('\uea77'),
	LOGOUT('\ue9ba'),
	NAVIGATION('\ue55d'),
	TARGET('\ue55c'),
	KEY('\ue73c'),
	LOCK('\ue897'),
	LOCK_OPEN('\ue898'),
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
	LAYERS('\ue53b'),
	PLAY('\ue037'),
	PAUSE('\ue034'),
	STOP('\ue047'),
	FAST_FORWARD('\ue01f'),
	FAST_REWIND('\ue020'),
	MUSIC_NOTE('\ue3a1'),
	REPEAT('\ue040'),
	FOLDER('\ue2c7'),
	STORAGE('\ue1db'),
	DATABASE('\uf20e'),
	ANCHOR('\uf1cd'),
	COPY('\ue14d'),
	CUT('\ue14e'),
	PASTE('\ue14f'),
	TEXT_DOCUMENT('\uf1c6'),
	BOLT('\uea0b'),
	TOGGLE_OFF('\ue9f5'),
	TOGGLE_ON('\ue9f6'),
	LIGHTBULB('\ue0f0'),
	LIGHTBULB_OFF('\ue9b8'),
	PASSKEY('\uf87f'),
	PASSWORD('\uf042'),
	PERSON('\ue7fd'),
	MORE('\ue5d3'),
	MORE_VERTICAL('\ue5d4'),
	NOTES('\uf562'),
	TOOLTIP('\ue9f8'),
	EXPAND('\uf830'),
	COLLAPSE('\uf507'),
	STACKS('\uf500'),

	;

	public static final ImIcons[] VALUES = values();

	public static final Lazy<List<ImIcon>> EXTRA_ICONS = Lazy.of(() -> {
		var list = new ArrayList<ImIcon>();
		NeoForge.EVENT_BUS.post(new ImIconsEvent(list::add));
		return list;
	});

	public final char icon;
	private final String iconString;

	ImIcons(char icon) {
		this.icon = icon;
		this.iconString = String.valueOf(icon);
	}

	@Override
	public String iconName() {
		return name();
	}

	@Override
	public char toChar() {
		return icon;
	}

	@Override
	public String toString() {
		return iconString;
	}
}
