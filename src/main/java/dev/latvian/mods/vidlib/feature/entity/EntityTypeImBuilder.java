package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import imgui.type.ImString;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityTypeImBuilder implements ImBuilder<EntityType<?>> {
	public static final Lazy<List<EntityType<?>>> ENTITY_TYPES = Lazy.of(() -> BuiltInRegistries.ENTITY_TYPE.stream().toList());

	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final EntityType<?>[] entityType = new EntityType[1];

	public EntityTypeImBuilder(@Nullable EntityType<?> defaultType) {
		this.entityType[0] = defaultType;
	}

	@Override
	public void set(EntityType<?> value) {
		entityType[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return graphics.combo("###entity-type", entityType, ENTITY_TYPES.get(), e -> I18n.get(e.getDescriptionId()), SEARCH);
	}

	@Override
	public boolean isValid() {
		return entityType[0] != null;
	}

	@Override
	public EntityType<?> build() {
		return entityType[0];
	}
}
