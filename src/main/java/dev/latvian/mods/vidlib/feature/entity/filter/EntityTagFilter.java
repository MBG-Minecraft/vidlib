package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;

public record EntityTagFilter(String tag) implements EntityFilter {
	public static SimpleRegistryType<EntityTagFilter> TYPE = SimpleRegistryType.dynamic("tags", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("tags").forGetter(EntityTagFilter::tag)
	).apply(instance, EntityTagFilter::new)), ByteBufCodecs.STRING_UTF8.map(EntityTagFilter::new, EntityTagFilter::tag));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("Tag", Builder::new);

		public final ImString tag = ImGuiUtils.resizableString();

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.inputText("###type-tag", tag);
			return ImUpdate.itemEdit();
		}

		@Override
		public boolean isValid() {
			return tag.isNotEmpty();
		}

		@Override
		public EntityFilter build() {
			return new EntityTagFilter(tag.get());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entity.getTags().contains(tag);
	}
}
