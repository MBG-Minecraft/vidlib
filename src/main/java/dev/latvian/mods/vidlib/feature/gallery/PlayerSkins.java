package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import dev.latvian.mods.vidlib.util.MiscUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface PlayerSkins {
	@ClientAutoRegister
	Gallery<UUID> GALLERY = Gallery.ofUUIDKey("player_skins", () -> VidLibPaths.USER.get().resolve("player-skins"), TriState.TRUE);

	static PlayerSkin of(SkinTexture skin) {
		return new PlayerSkin(skin.asset(), null, null, skin.slim() ? PlayerModelType.SLIM : PlayerModelType.WIDE, true);
	}

	static ClientAsset.ResourceTexture resourceTexture(Identifier texturePath) {
		var path = texturePath.getPath();
		return path.startsWith("textures/") && path.endsWith(".png") ? MiscUtils.assetFromPNG(texturePath) : new ClientAsset.ResourceTexture(texturePath);
	}

	@Nullable
	static ClientAsset.ResourceTexture nullableResourceTexture(@Nullable Identifier texturePath) {
		return texturePath == null ? null : resourceTexture(texturePath);
	}

	@Nullable
	static Identifier texturePath(@Nullable ClientAsset.Texture texture) {
		return texture == null ? null : texture.texturePath();
	}

	static SkinTexture skinTexture(PlayerSkin skin) {
		return new SkinTexture(resourceTexture(skin.body().texturePath()), skin.model() == PlayerModelType.SLIM);
	}

	static String textureUrl(PlayerSkin skin) {
		return skin.body() instanceof ClientAsset.DownloadedTexture downloaded ? downloaded.url() : "";
	}

	static ClientAsset.Texture bodyTexture(SkinTexture skin, String textureUrl) {
		return textureUrl.isEmpty() ? skin.asset() : new ClientAsset.DownloadedTexture(skin.asset().texturePath(), textureUrl);
	}

	PlayerSkin[] DEFAULT_WIDE_SKINS = new PlayerSkin[]{
		of(SkinTexture.WIDE_STEVE),
		of(SkinTexture.WIDE_ALEX),
		of(SkinTexture.WIDE_ARI),
		of(SkinTexture.WIDE_EFE),
		of(SkinTexture.WIDE_KAI),
		of(SkinTexture.WIDE_MAKENA),
		of(SkinTexture.WIDE_NOOR),
		of(SkinTexture.WIDE_SUNNY),
		of(SkinTexture.WIDE_ZURI)
	};

	PlayerSkin[] DEFAULT_SLIM_SKINS = new PlayerSkin[]{
		of(SkinTexture.SLIM_STEVE),
		of(SkinTexture.SLIM_ALEX),
		of(SkinTexture.SLIM_ARI),
		of(SkinTexture.SLIM_EFE),
		of(SkinTexture.SLIM_KAI),
		of(SkinTexture.SLIM_MAKENA),
		of(SkinTexture.SLIM_NOOR),
		of(SkinTexture.SLIM_SUNNY),
		of(SkinTexture.SLIM_ZURI)
	};

	Codec<PlayerSkin> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		SkinTexture.CODEC.fieldOf("skin").forGetter(PlayerSkins::skinTexture),
		Codec.STRING.optionalFieldOf("textureUrl", "").forGetter(PlayerSkins::textureUrl),
		ID.CODEC.optionalFieldOf("capeTexture").forGetter(s -> Optional.ofNullable(texturePath(s.cape()))),
		ID.CODEC.optionalFieldOf("elytraTexture").forGetter(s -> Optional.ofNullable(texturePath(s.elytra())))
	).apply(instance, (skin, textureUrl, capeTexture, elytraTexture) -> new PlayerSkin(
		bodyTexture(skin, textureUrl),
		capeTexture.map(PlayerSkins::resourceTexture).orElse(null),
		elytraTexture.map(PlayerSkins::resourceTexture).orElse(null),
		skin.slim() ? PlayerModelType.SLIM : PlayerModelType.WIDE,
		true
	)));

	StreamCodec<ByteBuf, PlayerSkin> STREAM_CODEC = CompositeStreamCodec.of(
		SkinTexture.STREAM_CODEC, PlayerSkins::skinTexture,
		ByteBufCodecs.STRING_UTF8, PlayerSkins::textureUrl,
		KLibStreamCodecs.optional(ID.STREAM_CODEC, null), s -> texturePath(s.cape()),
		KLibStreamCodecs.optional(ID.STREAM_CODEC, null), s -> texturePath(s.elytra()),
		(skin, textureUrl, capeTexture, elytraTexture) -> new PlayerSkin(
			bodyTexture(skin, textureUrl),
			nullableResourceTexture(capeTexture),
			nullableResourceTexture(elytraTexture),
			skin.slim() ? PlayerModelType.SLIM : PlayerModelType.WIDE,
			true
		)
	);

	DataType<PlayerSkin> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, PlayerSkin.class);

	static GalleryImage<UUID> get(Minecraft mc, UUID uuid) {
		return GALLERY.getRemote(mc, uuid, PlayerProfiles::getName, (id, n) -> PlayerProfiles.get(id).skinUrl().orElse(null), ImagePreProcessor.NONE);
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(DEFAULT_WIDE_SKINS[0].body().texturePath());
		}

		return get(mc, uuid).load(mc, false);
	}

	static PlayerModelType getModelType(@Nullable PlayerProfile profile) {
		if (profile == null || profile.isError()) {
			return PlayerModelType.WIDE;
		}

		return profile.slimModel() ? PlayerModelType.SLIM : PlayerModelType.WIDE;
	}

	static PlayerSkin getSkin(Minecraft mc, UUID uuid, boolean blocking) {
		if (uuid.equals(PlayerProfile.STEVE.profile().id())) {
			return DEFAULT_WIDE_SKINS[0];
		} else if (uuid.equals(PlayerProfile.ALEX.profile().id())) {
			return DEFAULT_SLIM_SKINS[1];
		} else {
			var profile = PlayerProfiles.get(uuid);
			var modelType = PlayerSkins.getModelType(profile);
			var skin = PlayerSkins.get(mc, uuid);
			skin.load(mc, blocking);
			ClientAsset.Texture body = profile == null ? resourceTexture(skin.textureId()) : profile.skinUrl()
				.filter(url -> !url.isBlank())
				.<ClientAsset.Texture>map(url -> new ClientAsset.DownloadedTexture(skin.textureId(), url))
				.orElseGet(() -> resourceTexture(skin.textureId()));
			return new PlayerSkin(body, null, null, modelType, true);
		}
	}
}
