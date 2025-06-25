package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolderList;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.NeoForge;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public interface ParticleOptionsImBuilder extends ImBuilder<ParticleOptions> {
	Lazy<ImBuilderHolderList<ParticleOptions>> IMGUI_BUILDERS = Lazy.of(() -> {
		var map = new LinkedHashMap<ParticleType<?>, Supplier<ParticleOptionsImBuilder>>();

		for (var type : BuiltInRegistries.PARTICLE_TYPE) {
			if (type instanceof ParticleOptions options) {
				map.put(type, () -> new Simple(options));
			} else {
				map.put(type, () -> new NBT(type));
			}
		}

		NeoForge.EVENT_BUS.post(new ParticleOptionsImBuilderRegistryEvent(map));

		var list = new ImBuilderHolderList<ParticleOptions>();

		for (var entry : map.entrySet()) {
			list.add(new ImBuilderHolder<>(BuiltInRegistries.PARTICLE_TYPE.getKey(entry.getKey()).toString(), Cast.to(entry.getValue())));
		}

		return list;
	});

	static ImBuilderWrapper<ParticleOptions> create() {
		return new ImBuilderWrapper<>(IMGUI_BUILDERS.get());
	}

	record Simple(ParticleOptions options) implements ParticleOptionsImBuilder {
		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return ImUpdate.NONE;
		}

		@Override
		public ParticleOptions build() {
			return options;
		}
	}

	record NBT(Codec<ParticleOptions> codec, ImString nbtString) implements ParticleOptionsImBuilder {
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
