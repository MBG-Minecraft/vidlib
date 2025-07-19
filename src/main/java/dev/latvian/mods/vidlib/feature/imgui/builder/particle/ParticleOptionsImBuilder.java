package dev.latvian.mods.vidlib.feature.imgui.builder.particle;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolderList;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.NeoForge;

import java.util.LinkedHashMap;
import java.util.List;

public interface ParticleOptionsImBuilder<T extends ParticleOptions> extends ImBuilder<T> {
	interface Factory<T extends ParticleOptions> {
		ParticleOptionsImBuilder<T> create(ParticleType<T> type);
	}

	Lazy<ImBuilderHolderList<ParticleOptions>> IMGUI_BUILDERS = Lazy.of(() -> {
		var map = new LinkedHashMap<ParticleType<?>, Factory<?>>();

		for (var type : BuiltInRegistries.PARTICLE_TYPE) {
			if (type instanceof ParticleOptions options) {
				map.put(type, t -> new Simple(options));
			} else {
				map.put(type, NBT::new);
			}
		}

		var event = new ParticleOptionsImBuilderRegistryEvent(map);

		event.register(List.of(
			ParticleTypes.BLOCK,
			ParticleTypes.BLOCK_MARKER,
			ParticleTypes.FALLING_DUST,
			ParticleTypes.DUST_PILLAR,
			ParticleTypes.BLOCK_CRUMBLE
		), BlockParticleOptionImBuilder::new);

		event.register(ParticleTypes.DUST, t -> new DustParticleOptionImBuilder());

		event.register(List.of(
			ParticleTypes.ENTITY_EFFECT,
			ParticleTypes.TINTED_LEAVES
		), ColorParticleOptionImBuilder::new);

		NeoForge.EVENT_BUS.post(event);

		var list = new ImBuilderHolderList<ParticleOptions>();

		for (var entry : map.entrySet()) {
			list.add(new ImBuilderHolder<>(BuiltInRegistries.PARTICLE_TYPE.getKey(entry.getKey()).toString(), () -> entry.getValue().create(Cast.to(entry.getKey()))));
		}

		return list;
	});

	static ImBuilderWrapper<ParticleOptions> create() {
		return new ImBuilderWrapper<>(IMGUI_BUILDERS.get());
	}

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
