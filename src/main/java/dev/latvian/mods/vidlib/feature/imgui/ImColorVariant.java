package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.Color;
import net.minecraft.util.StringRepresentable;

public enum ImColorVariant implements StringRepresentable {
	DEFAULT("default", "Default", Color.of(0x664296FA), Color.ofRGB(0x4296FA), Color.ofRGB(0x0F87FA), Color.WHITE),
	GRAY("gray", "Gray", Color.hsb(0F, 0F, 0.65F, 150), Color.hsb(0F, 0F, 0.65F, 255), Color.hsb(0F, 0F, 0.75F, 255), Color.WHITE),
	RED("red", "Red", 0),
	ORANGE("orange", "Orange", 30),
	YELLOW("yellow", "Yellow", 55),
	LIME("lime", "Lime", 84),
	GREEN("green", "Green", 124),
	TEAL("teal", "Teal", 165),
	CYAN("cyan", "Cyan", 180),
	BLUE("blue", "Blue", 205),
	DARK_BLUE("dark_blue", "Dark Blue", 225),
	DARK_PURPLE("dark_purple", "Dark Purple", 255),
	PURPLE("purple", "Purple", 280),
	MAGENTA("magenta", "Magenta", 300),
	ROSE("rose", "Rose", 330),

	;

	public static final ImColorVariant[] VALUES = values();

	public final String id;
	public final String displayName;
	public final Color color;
	public final Color hoverColor;
	public final Color activeColor;
	public final Color textColor;

	ImColorVariant(String id, String displayName, Color color, Color hoverColor, Color activeColor, Color textColor) {
		this.id = id;
		this.displayName = displayName;
		this.color = color;
		this.hoverColor = hoverColor;
		this.activeColor = activeColor;
		this.textColor = textColor;
	}

	ImColorVariant(String id, String displayName, int hueAngle) {
		this.id = id;
		this.displayName = displayName;
		float hue = hueAngle / 360F;
		this.color = Color.hsb(hue, 0.8F, 0.8F, 150);
		this.hoverColor = Color.hsb(hue, 0.8F, 0.8F, 255);
		this.activeColor = Color.hsb(hue, 0.9F, 0.9F, 255);
		this.textColor = Color.hsb(hue, 0.67F, 1F, 255);
	}

	@Override
	public String getSerializedName() {
		return id;
	}
}
