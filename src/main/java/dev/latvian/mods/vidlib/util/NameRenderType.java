package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.Tristate;
import net.minecraft.util.StringRepresentable;

public enum NameRenderType implements StringRepresentable {
	DEFAULT("default", Tristate.TRUE, Tristate.DEFAULT),
	ONLY_NAME("only_name", Tristate.TRUE, Tristate.FALSE),
	ONLY_HEALTH("only_health", Tristate.FALSE, Tristate.DEFAULT),
	HIDDEN("hidden", Tristate.FALSE, Tristate.FALSE),
	VANILLA("vanilla", Tristate.DEFAULT, Tristate.DEFAULT);

	public static final NameRenderType[] VALUES = values();
	public static final DataType<NameRenderType> DATA_TYPE = DataType.of(VALUES);

	private final String name;
	public final Tristate renderName;
	public final Tristate renderHealth;

	NameRenderType(String name, Tristate renderName, Tristate renderHealth) {
		this.name = name;
		this.renderName = renderName;
		this.renderHealth = renderHealth;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}