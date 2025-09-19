package dev.latvian.mods.vidlib.feature.imgui.icon;

import dev.latvian.mods.klib.util.Lazy;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public enum ImIcons implements ImIcon {
	CATEGORY_SHAPES("Shapes"),
	SQUARE('\ueb36'),
	CIRCLE('\uef4a'),
	HEXAGON('\ueb39'),
	ASTERIX('\ue1eb'),
	STAR('\ue838'),
	HEART('\ue87d'),

	CATEGORY_UI("UI"),
	TOGGLE_OFF('\ue9f5'),
	TOGGLE_ON('\ue9f6'),
	UNDO('\ue166'),
	REDO('\ue15a'),
	ARROW_LEFT('\ue5c4'),
	ARROW_RIGHT('\ue5c8'),
	ARROW_UP('\ue5d8'),
	ARROW_DOWN('\ue5db'),
	BLOCK('\ue14b'),
	ROTATE('\ue577'),
	RECYCLE('\ue760'),
	LOGIN('\uea77'),
	LOGOUT('\ue9ba'),
	SWAP('\ue8d4'),
	EXPAND('\uf830'),
	COLLAPSE('\uf507'),
	REPEAT('\ue040'),
	MORE('\ue5d3'),
	MORE_VERTICAL('\ue5d4'),
	DELETE('\ue872'),
	BACKSPACE('\ue14a'),
	EDIT('\ue3c9'),
	OPEN('\ue2c8'),
	CODE('\ue86f'),
	VISIBLE('\ue8f4'),
	INVISIBLE('\ue8f5'),
	ADD('\ue145'),
	ADD_CIRCLE('\ue147'),
	REMOVE('\ue15b'),
	CLOSE('\ue5cd'),
	CHECK('\ue5ca'),
	CHECK_CIRCLE('\ue86c'),
	SEARCH('\ue8b6'),
	QUESTION('\ueb8b'),
	COPY('\ue14d'),
	CUT('\ue14e'),
	PASTE('\ue14f'),
	NUMBERS('\ueac7'),
	PERCENT('\ueb58'),
	MENU('\ue5d2'),
	DOWNLOAD('\uf090'),
	SELECT('\uf74d'),
	ERROR('\ue000'),
	WARNING('\ue002'),
	WRENCH('\ue869'),
	SETTINGS('\ue8b8'),
	RELOAD('\ue86a'),
	FILTER('\uef4f'),
	SORT('\ue164'),
	LABEL('\ue892'),
	NO_LABEL('\ue9b6'),
	FULLSCREEN('\ue5d0'),
	LAYERS('\ue53b'),
	STACKS('\uf500'),
	WIDGETS('\ue1bd'),
	DASHBOARD('\ue871'),
	POLYLINE('\uebbb'),
	FLOWCHART('\uf38d'),
	GRAPH('\uf3a0'),
	SLASH('\uf753'),

	CATEGORY_MEDIA("Media"),
	PLAY('\ue037'),
	PAUSE('\ue034'),
	STOP('\ue047'),
	PLAY_CIRCLE('\ue1c4'),
	PAUSE_CIRCLE('\ue1a2'),
	STOP_CIRCLE('\uef71'),
	FAST_REWIND('\ue020'),
	FAST_FORWARD('\ue01f'),
	MUSIC_NOTE('\ue3a1'),
	MUSIC_NOTE_OFF('\ue440'),
	TUNE('\ue429'),

	CATEGORY_FILES("Files"),
	HOME('\ue88a'),
	OPEN_IN_NEW('\ue89e'),
	DOCUMENT('\ue873'),
	FOLDER('\ue2c7'),
	STORAGE('\ue1db'),
	DATABASE('\uf20e'),
	TEXT_DOCUMENT('\uf1c6'),
	NOTES('\uf562'),

	CATEGORY_ENVIRONMENT("Environment"),
	SUN('\ue518'),
	MOON('\ue51c'),
	FREEZE('\ueb3b'),
	FIRE('\uef55'),
	ROCKET('\ueba5'),
	ROCKET_LAUNCH('\ueb9b'),
	EXPLOSION('\uf685'),
	BOMB('\uf568'),
	VOLCANO('\uebda'),
	SCIENCE('\uea4b'),
	SCIENCE_OFF('\uf542'),
	EXPERIMENT('\ue686'),
	HIVE('\ueaa6'),
	EGG('\ueacc'),
	PAW('\ue91d'),
	BONE('\uefb1'),
	BUG('\ue868'),
	WORLD('\ue80b'),
	LEAF('\uf8be'),
	TREE('\uea63'),
	BOLT('\uea0b'),

	CATEGORY_MISC("Misc"),
	CAMERA('\ue04b'),
	APERTURE('\ue3af'),
	SPEED('\ue9e4'),
	TIMELAPSE('\ue422'),
	LOCATION('\ue55f'),
	MEMORY('\ue322'),
	NAVIGATION('\ue55d'),
	TARGET('\ue55c'),
	KEY('\ue73c'),
	LOCK('\ue897'),
	LOCK_OPEN('\ue898'),
	ACCOUNT('\ue853'),
	SCHEDULE('\ue8b5'),
	PAID('\uf041'),
	MONEY('\ue227'),
	FRAMED_CUBE('\ue9fe'),
	SWITCH_ACCESS('\uf6fd'),
	BACKPACK('\uf19c'),
	BACKPACK_OFF('\uf237'),
	ANCHOR('\uf1cd'),
	LIGHTBULB('\ue0f0'),
	LIGHTBULB_OFF('\ue9b8'),
	PASSKEY('\uf87f'),
	PASSWORD('\uf042'),
	PERSON('\ue7fd'),
	TOOLTIP('\ue9f8'),
	DIAMOND('\uead5'),
	ANIMATION('\ue71c'),
	BLUR('\uf029'),
	FLAG('\ue153'),
	SHIELD('\ue9e0'),
	PALETTE('\ue40a'),

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

	ImIcons(String category) {
		this.icon = 0;
		this.iconString = category;
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
