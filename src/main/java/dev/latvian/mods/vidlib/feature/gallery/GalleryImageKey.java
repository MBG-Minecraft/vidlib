package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.latvian.mods.klib.codec.FlatMapCodec;

public record GalleryImageKey<T>(Gallery<T> gallery, T id) {
	public static final MapCodec<GalleryImageKey<?>> MAP_CODEC = new FlatMapCodec<>(
		"gallery",
		"id",
		Gallery.CODEC,
		g -> g.identifierCodec,
		GalleryImageKey::gallery,
		GalleryImageKey::id,
		(gallery, id) -> new GalleryImageKey(gallery, id)
	);

	public static final Codec<GalleryImageKey<?>> CODEC = MAP_CODEC.codec();

	public GalleryImage<T> image() {
		return gallery.get(id);
	}
}
