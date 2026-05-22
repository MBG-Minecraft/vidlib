package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;

import java.util.List;

@AutoInit
public interface Tracksuits {
	static Clothing tracksuit(String id) {
		return new Clothing(ID.video("tracksuit/" + id), ClothingParts.NO_HEAD);
	}

	Clothing BLACK = tracksuit("black");
	Clothing WHITE = tracksuit("white");
	Clothing RED = tracksuit("red");
	Clothing PINK = tracksuit("pink");
	Clothing MAGENTA = tracksuit("magenta");
	Clothing PURPLE = tracksuit("purple");
	Clothing BLUE = tracksuit("blue");
	Clothing CYAN = tracksuit("cyan");
	Clothing GREEN = tracksuit("green");
	Clothing LIME = tracksuit("lime");
	Clothing YELLOW = tracksuit("yellow");
	Clothing ORANGE = tracksuit("orange");

	List<Clothing> COLORED = List.of(
		RED,
		PINK,
		MAGENTA,
		PURPLE,
		BLUE,
		CYAN,
		GREEN,
		LIME,
		YELLOW,
		ORANGE
	);

	Clothing SQUID = tracksuit("squid");

	Clothing X = new Clothing(ID.video("tracksuit/x"), ClothingParts.ONLY_LEGS);
}