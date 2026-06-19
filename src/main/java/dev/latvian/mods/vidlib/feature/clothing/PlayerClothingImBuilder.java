package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;

public class PlayerClothingImBuilder implements ImBuilder<PlayerClothing> {
	public static final ImBuilderType<PlayerClothing> TYPE = PlayerClothingImBuilder::new;
	public static final Function<ResourceKey<ClothingSet>, String> PRESET_NAME = key -> key.identifier().toString();

	public final PlayerClothing.Type[] type = {null};
	public final EnumImBuilder<ResourceKey<ClothingSet>> presetBuilder = new EnumImBuilder<>(ClothingPresets.INSTANCE.map.keySet().stream().sorted((o1, o2) -> o1.identifier().compareNamespaced(o2.identifier())).toList(), Tracksuits.BLUE);
	public final ClothingSetImBuilder customBuilder = new ClothingSetImBuilder();

	public PlayerClothingImBuilder() {
		presetBuilder.nameGetter = PRESET_NAME;
	}

	@Override
	public void set(PlayerClothing value) {
		if (type[0] == null) {
			type[0] = value == null ? PlayerClothing.Type.NONE : value.type();
		}

		if (value != null) {
			presetBuilder.set(value.preset());
			customBuilder.set(value.custom());
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = graphics.combo("###player-clothing-type", type, "", PlayerClothing.Type.VALUES, t -> t.displayName);

		switch (type[0]) {
			case PRESET -> update = update.or(presetBuilder.imgui(graphics));
			case CUSTOM -> update = update.or(customBuilder.imgui(graphics));
		}

		return update;
	}

	@Override
	public PlayerClothing build() {
		return switch (type[0]) {
			case NONE -> PlayerClothing.NONE;
			case PRESET -> PlayerClothing.preset(presetBuilder.build());
			case CUSTOM -> PlayerClothing.custom(customBuilder.build());
			case null -> PlayerClothing.NONE;
		};
	}

	@Override
	public boolean isValid() {
		return switch (type[0]) {
			case NONE -> true;
			case PRESET -> presetBuilder.isValid();
			case CUSTOM -> customBuilder.isValid();
			case null -> false;
		};
	}
}
