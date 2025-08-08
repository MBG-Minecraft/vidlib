package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class ClothingImBuilder implements ImBuilder<Clothing> {
	public static final ImBuilderType<Clothing> TYPE = ClothingImBuilder::new;

	public final EnumImBuilder<ResourceLocation> clothing = new EnumImBuilder<>(ClothingCommand.CLOTHING_IDS);
	public final BooleanImBuilder head = new BooleanImBuilder();
	public final BooleanImBuilder body = new BooleanImBuilder();
	public final BooleanImBuilder legs = new BooleanImBuilder();
	public final BooleanImBuilder feet = new BooleanImBuilder();
	public final BooleanImBuilder enchanted = new BooleanImBuilder();

	public ClothingImBuilder() {
		this.clothing.set(Clothing.BLUE_TRACKSUIT.id().location());
		this.head.set(true);
		this.body.set(true);
		this.legs.set(true);
		this.feet.set(true);
		this.enchanted.set(false);
	}

	@Override
	public void set(Clothing value) {
		if (value != null) {
			clothing.set(value.id().location());
			head.set(value.parts().head());
			body.set(value.parts().body());
			legs.set(value.parts().legs());
			feet.set(value.parts().feet());
			enchanted.set(value.parts().enchanted());
		} else {
			clothing.set(null);
			head.set(true);
			body.set(true);
			legs.set(true);
			feet.set(true);
			enchanted.set(false);
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = clothing.imgui(graphics);
		update = update.or(clothing.imguiKey(graphics, "Clothing", "clothing"));
		update = update.or(head.imguiKey(graphics, "Head", "head"));
		update = update.or(body.imguiKey(graphics, "Body", "body"));
		update = update.or(legs.imguiKey(graphics, "Legs", "legs"));
		update = update.or(feet.imguiKey(graphics, "Feet", "feet"));
		update = update.or(enchanted.imguiKey(graphics, "Enchanted", "enchanted"));
		return update;
	}

	@Override
	public Clothing build() {
		return new Clothing(
			ResourceKey.create(EquipmentAssets.ROOT_ID, clothing.build()),
			new ClothingParts(
				head.build(),
				body.build(),
				legs.build(),
				feet.build(),
				enchanted.build()
			)
		);
	}

	@Override
	public boolean isValid() {
		return clothing.isValid();
	}
}
