package dev.latvian.mods.vidlib.feature.font;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.UnitType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public record TTFFile(UnitType<ByteBuf, TTFFile> type, Identifier resource) implements CustomRegistryValue<ByteBuf, TTFFile> {
	public static UnitType<ByteBuf, TTFFile> create(Identifier resource) {
		return UnitType.create(resource.getPath(), type -> new TTFFile(type, resource.withSuffix(".ttf")));
	}

	public static final UnitType<ByteBuf, TTFFile> MATERIAL_ICONS_ROUND_REGULAR = create(Identifier.fromNamespaceAndPath("imguiresources", "materialiconsround_regular"));
	public static final UnitType<ByteBuf, TTFFile> JETBRAINS_MONO_REGULAR = create(Identifier.fromNamespaceAndPath("imguiresources", "jetbrainsmono_regular"));

	public static final CustomRegistry<ByteBuf, TTFFile> REGISTRY = CustomRegistry.create("ttf");
	public static final Codec<Ref<TTFFile>> CODEC = REGISTRY.codec();
	public static final StreamCodec<ByteBuf, Ref<TTFFile>> STREAM_CODEC = REGISTRY.streamCodec();
	public static final DataType<Ref<TTFFile>> DATA_TYPE = REGISTRY.dataType();

	public static void builtInTypes(CustomRegistryTypeCollector<ByteBuf, TTFFile> registry) {
		registry.register(MATERIAL_ICONS_ROUND_REGULAR);
		registry.register(JETBRAINS_MONO_REGULAR);
	}

	@Override
	public CustomRegistry<ByteBuf, TTFFile> getRegistry() {
		return REGISTRY;
	}

	public byte[] load(ResourceManager resourceManager) throws IOException {
		try (var in = resourceManager.getResource(resource).orElseThrow().open()) {
			return in.readAllBytes();
		} catch (Exception e) {
			throw new RuntimeException("Failed to read TTF file: " + resource, e);
		}
	}
}
