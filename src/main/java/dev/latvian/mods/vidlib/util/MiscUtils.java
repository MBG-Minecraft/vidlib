package dev.latvian.mods.vidlib.util;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.util.net.HttpResponseData;
import dev.latvian.mods.klib.util.net.NetUtils;
import dev.latvian.mods.vidlib.VidLib;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public interface MiscUtils {
	Comparator<GameProfile> PROFILE_COMPARATOR = (a, b) -> a.getName().compareToIgnoreCase(b.getName());

	RegistryAccess STATIC_REGISTRY_ACCESS = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

	Mutable<Supplier<Player>> CLIENT_PLAYER = new MutableObject<>(() -> null);

	Set<Item> NO_BOB_ITEMS = new ReferenceArraySet<>(Set.of(
		Items.COMPASS,
		Items.RECOVERY_COMPASS
	));

	static Path createDir(Path path) {
		if (Files.notExists(path)) {
			try {
				Files.createDirectories(path);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to create directory " + path.toAbsolutePath());
				throw new RuntimeException(ex);
			}
		}

		return path;
	}

	static <T> SequencedCollection<T> toSequencedCollection(Iterable<T> collection) {
		if (collection instanceof SequencedCollection) {
			return (SequencedCollection<T>) collection;
		}

		var list = new ArrayList<T>();

		for (var element : collection) {
			list.add(element);
		}

		return list;
	}

	static int size(Iterable<?> iterable) {
		if (iterable instanceof Collection) {
			return ((Collection<?>) iterable).size();
		}

		int size = 0;

		for (var ignored : iterable) {
			size++;
		}

		return size;
	}

	static boolean isEmpty(Iterable<?> iterable) {
		if (iterable instanceof Collection) {
			return ((Collection<?>) iterable).isEmpty();
		}

		return iterable.iterator().hasNext();
	}

	static DataResult<byte[]> fetch(URI uri) {
		HttpResponseData response = null;

		try {
			response = NetUtils.send(NetUtils.newRequest().uri(uri).GET().build(), true);
			return DataResult.success(response.data);
		} catch (Exception ex) {
			if (response != null) {
				var res = response;
				return DataResult.error(() -> "Error " + res.code + ": " + ex);
			} else {
				return DataResult.error(ex::toString);
			}
		}
	}

	static <T> T[] fastIndexedLookup(T[] values, ToIntFunction<T> idGetter, IntFunction<T[]> arrayConstructor) {
		int max = 0;

		for (var type : values) {
			max = Math.max(max, idGetter.applyAsInt(type));
		}

		var lookup = arrayConstructor.apply(max + 1);

		for (var type : values) {
			lookup[idGetter.applyAsInt(type)] = type;
		}

		return lookup;
	}

	static ClientAsset assetFromPNG(ResourceLocation png) {
		return new ClientAsset(png.withPath(png.getPath().substring(9, png.getPath().length() - 4)));
	}
}
