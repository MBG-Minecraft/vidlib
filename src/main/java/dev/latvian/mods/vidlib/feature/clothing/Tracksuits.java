package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;

import java.util.List;

@AutoInit
public interface Tracksuits {
	Clothing BLACK = new Clothing(Clothing.createKey("tracksuit/black"));
	Clothing WHITE = new Clothing(Clothing.createKey("tracksuit/white"));
	Clothing RED = new Clothing(Clothing.createKey("tracksuit/red"));
	Clothing PINK = new Clothing(Clothing.createKey("tracksuit/pink"));
	Clothing MAGENTA = new Clothing(Clothing.createKey("tracksuit/magenta"));
	Clothing PURPLE = new Clothing(Clothing.createKey("tracksuit/purple"));
	Clothing BLUE = new Clothing(Clothing.createKey("tracksuit/blue"));
	Clothing CYAN = new Clothing(Clothing.createKey("tracksuit/cyan"));
	Clothing GREEN = new Clothing(Clothing.createKey("tracksuit/green"));
	Clothing LIME = new Clothing(Clothing.createKey("tracksuit/lime"));
	Clothing YELLOW = new Clothing(Clothing.createKey("tracksuit/yellow"));
	Clothing ORANGE = new Clothing(Clothing.createKey("tracksuit/orange"));

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

	Clothing SQUID = new Clothing(Clothing.createKey("tracksuit/squid"));
}