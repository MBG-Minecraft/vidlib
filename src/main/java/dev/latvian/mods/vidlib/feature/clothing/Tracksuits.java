package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;

import java.util.List;

@AutoInit
public interface Tracksuits {
	Clothing BLACK = new Clothing(ID.video("tracksuit/black"));
	Clothing WHITE = new Clothing(ID.video("tracksuit/white"));
	Clothing RED = new Clothing(ID.video("tracksuit/red"));
	Clothing PINK = new Clothing(ID.video("tracksuit/pink"));
	Clothing MAGENTA = new Clothing(ID.video("tracksuit/magenta"));
	Clothing PURPLE = new Clothing(ID.video("tracksuit/purple"));
	Clothing BLUE = new Clothing(ID.video("tracksuit/blue"));
	Clothing CYAN = new Clothing(ID.video("tracksuit/cyan"));
	Clothing GREEN = new Clothing(ID.video("tracksuit/green"));
	Clothing LIME = new Clothing(ID.video("tracksuit/lime"));
	Clothing YELLOW = new Clothing(ID.video("tracksuit/yellow"));
	Clothing ORANGE = new Clothing(ID.video("tracksuit/orange"));

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

	Clothing SQUID = new Clothing(ID.video("tracksuit/squid"));
}