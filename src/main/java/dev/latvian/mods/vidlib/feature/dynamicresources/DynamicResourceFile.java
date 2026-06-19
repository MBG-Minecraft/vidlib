package dev.latvian.mods.vidlib.feature.dynamicresources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record DynamicResourceFile(Identifier template, String location) {
	public static final Codec<DynamicResourceFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("template").forGetter(DynamicResourceFile::template),
		Codec.STRING.fieldOf("location").forGetter(DynamicResourceFile::location)
	).apply(instance, DynamicResourceFile::new));
}
