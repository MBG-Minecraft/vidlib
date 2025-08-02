package dev.latvian.mods.vidlib.feature.imgui.builder.particle;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderSupplier;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public interface ParticleOptionsImBuilder<T extends ParticleOptions> extends ImBuilder<T> {
	interface Factory<T extends ParticleOptions> {
		ParticleOptionsImBuilder<T> create(ParticleType<T> type);
	}

	ImBuilderWrapper.Factory<ParticleOptions> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(ParticleOptionsImBuilderRegistryEvent::new);

	static ImBuilderWrapper<ParticleOptions> create() {
		return new ImBuilderWrapper<>(IMGUI_BUILDER_FACTORY);
	}

	ImBuilderSupplier<ParticleOptions> SUPPLIER = ParticleOptionsImBuilder::create;

	record Simple(ParticleOptions options) implements ParticleOptionsImBuilder<ParticleOptions> {
		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return ImUpdate.NONE;
		}

		@Override
		public ParticleOptions build() {
			return options;
		}
	}

	record NBT(Codec<ParticleOptions> codec, ImString nbtString) implements ParticleOptionsImBuilder<ParticleOptions> {
		public NBT(ParticleType<?> type) {
			this(Cast.to(type.codec().codec()), ImGuiUtils.resizableString());
			nbtString.set("{}");
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.inputText("###nbt", nbtString);
			return ImUpdate.itemEdit();
		}

		@Override
		public boolean isValid() {
			try {
				var mc = Minecraft.getInstance();
				var parser = mc.level.nbtParser();
				var tag = parser.parseFully(nbtString.get());
				return codec.decode(mc.level.nbtOps(), tag).isSuccess();
			} catch (Exception ex) {
				return false;
			}
		}

		@Override
		public ParticleOptions build() {
			try {
				var mc = Minecraft.getInstance();
				var parser = mc.level.nbtParser();
				var tag = parser.parseFully(nbtString.get());
				return codec.decode(mc.level.nbtOps(), tag).getOrThrow().getFirst();
			} catch (Exception ex) {
				return null;
			}
		}
	}
}
