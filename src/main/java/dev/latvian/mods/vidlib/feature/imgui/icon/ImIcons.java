package dev.latvian.mods.vidlib.feature.imgui.icon;

import dev.latvian.mods.klib.util.Lazy;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public enum ImIcons implements ImIcon {
	CATEGORY_SHAPES("Shapes"),
	ASTERIX('\ue1eb'),
	CIRCLE('\uef4a'),
	HEART('\ue87d'),
	HEXAGON('\ueb39'),
	SQUARE('\ueb36'),
	STAR('\ue838'),

	CATEGORY_UI("UI"),
	ABC('\ueb94'),
	ADD('\ue145'),
	ADD_CIRCLE('\ue147'),
	ARROW_DOWN('\ue5db'),
	ARROW_LEFT('\ue5c4'),
	ARROW_RIGHT('\ue5c8'),
	ARROW_UP('\ue5d8'),
	BACKSPACE('\ue14a'),
	BLOCK('\ue14b'),
	CHECK('\ue5ca'),
	CHECK_CIRCLE('\ue86c'),
	CLOSE('\ue5cd'),
	CODE('\ue86f'),
	CODE_OFF('\ue4f3'),
	COLLAPSE('\uf507'),
	COPY('\ue14d'),
	CUT('\ue14e'),
	DASHBOARD('\ue871'),
	DOWNLOAD('\uf090'),
	DRAG_INDICATOR('\ue945'),
	EDIT('\ue3c9'),
	ERROR('\ue000'),
	EXPAND('\uf830'),
	FILTER('\uef4f'),
	FILTER_OFF('\ueb32'),
	FLOWCHART('\uf38d'),
	FULLSCREEN('\ue5d0'),
	GRAPH('\uf3a0'),
	INVISIBLE('\ue8f5'),
	LABEL('\ue892'),
	LAYERS('\ue53b'),
	LOGIN('\uea77'),
	LOGOUT('\ue9ba'),
	MENU('\ue5d2'),
	MORE('\ue5d3'),
	MORE_VERTICAL('\ue5d4'),
	NO_LABEL('\ue9b6'),
	NUMBERS('\ueac7'),
	OPEN('\ue2c8'),
	PASTE('\ue14f'),
	PERCENT('\ueb58'),
	POLYLINE('\uebbb'),
	QUESTION('\ueb8b'),
	RECYCLE('\ue760'),
	REDO('\ue15a'),
	RELOAD('\ue86a'),
	REMOVE('\ue15b'),
	REPEAT('\ue040'),
	ROTATE('\ue577'),
	SEARCH('\ue8b6'),
	SELECT('\uf74d'),
	SETTINGS('\ue8b8'),
	SLASH('\uf753'),
	SORT('\ue164'),
	SPLIT_SCREEN('\uf06d'),
	STACKS('\uf500'),
	SWAP('\ue8d4'),
	TOGGLE_OFF('\ue9f5'),
	TOGGLE_ON('\ue9f6'),
	TRASHCAN('\ue872'),
	UNDO('\ue166'),
	UPLOAD('\uf09b'),
	VISIBLE('\ue8f4'),
	WARNING('\ue002'),
	WIDGETS('\ue1bd'),
	WRENCH('\ue869'),

	CATEGORY_MEDIA("Media"),
	FAST_FORWARD('\ue01f'),
	FAST_REWIND('\ue020'),
	MUSIC_NOTE('\ue3a1'),
	MUSIC_NOTE_OFF('\ue440'),
	PAUSE('\ue034'),
	PAUSE_CIRCLE('\ue1a2'),
	PLAY('\ue037'),
	PLAY_CIRCLE('\ue1c4'),
	STOP('\ue047'),
	STOP_CIRCLE('\uef71'),
	TUNE('\ue429'),

	CATEGORY_FILES("Files"),
	DATABASE('\uf20e'),
	DOCUMENT('\ue873'),
	FOLDER('\ue2c7'),
	HOME('\ue88a'),
	NOTES('\uf562'),
	OPEN_IN_NEW('\ue89e'),
	STORAGE('\ue1db'),
	TEXT('\ue728'),
	TEXT_DOCUMENT('\uf1c6'),

	CATEGORY_ENVIRONMENT("Environment"),
	BOLT('\uea0b'),
	BOMB('\uf568'),
	BONE('\uefb1'),
	BUG('\ue868'),
	CLOUD('\ue2bd'),
	EGG('\ueacc'),
	EXPERIMENT('\ue686'),
	EXPLOSION('\uf685'),
	FIRE('\uef55'),
	FLUID('\ue798'),
	FOG('\ue176'),
	FREEZE('\ueb3b'),
	HIVE('\ueaa6'),
	LEAF('\uf8be'),
	MOON('\ue51c'),
	PAW('\ue91d'),
	ROCKET('\ueba5'),
	ROCKET_LAUNCH('\ueb9b'),
	SCIENCE('\uea4b'),
	SCIENCE_OFF('\uf542'),
	SUN('\ue518'),
	TREE('\uea63'),
	VOLCANO('\uebda'),
	WATER('\uf084'),
	WORLD('\ue80b'),

	CATEGORY_MISC("Misc"),
	ACCOUNT('\ue853'),
	ANCHOR('\uf1cd'),
	ANIMATION('\ue71c'),
	APERTURE('\ue3af'),
	BACKPACK('\uf19c'),
	BACKPACK_OFF('\uf237'),
	BLUR('\uf029'),
	CAMERA('\ue04b'),
	DIAMOND('\uead5'),
	FLAG('\ue153'),
	FOOTPRINT('\uf87d'),
	FRAMED_CUBE('\ue9fe'),
	HOURGLASS('\uebff'),
	KEEP_PIN('\ue6aa'),
	KEEP_PIN_OFF('\ue6f9'),
	KEY('\ue73c'),
	LIGHTBULB('\ue0f0'),
	LIGHTBULB_OFF('\ue9b8'),
	LOCATION('\ue55f'),
	LOCK('\ue897'),
	LOCK_OPEN('\ue898'),
	MEMORY('\ue322'),
	MONEY('\ue227'),
	NAVIGATION('\ue55d'),
	PAID('\uf041'),
	PALETTE('\ue40a'),
	PASSKEY('\uf87f'),
	PASSWORD('\uf042'),
	PERSON('\ue7fd'),
	SCHEDULE('\ue8b5'),
	SHIELD('\ue9e0'),
	SHIRT('\uef7b'),
	SPEED('\ue9e4'),
	SWITCH_ACCESS('\uf6fd'),
	SWORDS('\uf889'),
	TARGET('\ue55c'),
	TIMELAPSE('\ue422'),
	TIMER('\ue425'),
	TIMER_OFF('\ue426'),
	TOOLTIP('\ue9f8'),

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
