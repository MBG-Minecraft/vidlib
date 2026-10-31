package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.MiscUtils;
import dev.latvian.mods.klib.util.SkinTexture;
import dev.latvian.mods.klib.util.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.visual.SpriteKey;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface PlayerSkins {
	@ClientAutoRegister
	Gallery<UUID> GALLERY = Gallery.ofUUIDKey("player_skins", () -> VidLibPaths.USER.get().resolve("player-skins"), TriState.TRUE);

	static PlayerSkin of(SkinTexture skin) {
		return PlayerSkin.insecure(skin.asset(), null, null, skin.slim() ? PlayerModelType.SLIM : PlayerModelType.WIDE);
	}

	/**
	 * 26.3 dropped the {@code textureUrl} component of {@link PlayerSkin}: a remote skin is now
	 * expressed by wrapping it in a {@link ClientAsset.DownloadedTexture}.
	 */
	static String textureUrl(PlayerSkin skin) {
		return skin.body() instanceof ClientAsset.DownloadedTexture downloaded ? downloaded.url() : "";
	}

	private static Optional<ClientAsset.ResourceTexture> resourceTexture(ClientAsset.@Nullable Texture texture) {
		return Optional.ofNullable(texture).filter(ClientAsset.ResourceTexture.class::isInstance).map(ClientAsset.ResourceTexture.class::cast);
	}

	private static PlayerSkin skin(ClientAsset.ResourceTexture body, String textureUrl, ClientAsset.@Nullable ResourceTexture cape, ClientAsset.@Nullable ResourceTexture elytra, PlayerModelType modelType) {
		return PlayerSkin.insecure(
			textureUrl.isEmpty() ? body : new ClientAsset.DownloadedTexture(body.id(), textureUrl),
			cape,
			elytra,
			modelType
		);
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
		SkinTexture.CODEC.fieldOf("skin").forGetter(s -> new SkinTexture(SpriteKey.narrow(MiscUtils.assetFromPNG(s.body().texturePath())), s.model() == PlayerModelType.SLIM)),
		Codec.STRING.optionalFieldOf("textureUrl", "").forGetter(PlayerSkins::textureUrl),
		ClientAsset.ResourceTexture.CODEC.optionalFieldOf("capeTexture").forGetter(s -> resourceTexture(s.cape())),
		ClientAsset.ResourceTexture.CODEC.optionalFieldOf("elytraTexture").forGetter(s -> resourceTexture(s.elytra()))
	).apply(instance, (skin, textureUrl, capeTexture, elytraTexture) -> skin(
		skin.asset(),
		textureUrl,
		capeTexture.orElse(null),
		elytraTexture.orElse(null),
		skin.slim() ? PlayerModelType.SLIM : PlayerModelType.WIDE
	)));

	StreamCodec<ByteBuf, PlayerSkin> STREAM_CODEC = CompositeStreamCodec.of(
		SkinTexture.STREAM_CODEC, s -> new SkinTexture(SpriteKey.narrow(MiscUtils.assetFromPNG(s.body().texturePath())), s.model() == PlayerModelType.SLIM),
		ByteBufCodecs.STRING_UTF8, PlayerSkins::textureUrl,
		KLibStreamCodecs.optional(ClientAsset.ResourceTexture.STREAM_CODEC, null), s -> resourceTexture(s.cape()).orElse(null),
		KLibStreamCodecs.optional(ClientAsset.ResourceTexture.STREAM_CODEC, null), s -> resourceTexture(s.elytra()).orElse(null),
		(skin, textureUrl, capeTexture, elytraTexture) -> skin(
			skin.asset(),
			textureUrl,
			capeTexture,
			elytraTexture,
			skin.slim() ? PlayerModelType.SLIM : PlayerModelType.WIDE
		)
	);

	DataType<PlayerSkin> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC);

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
		var applied = getAppliedSkin(mc, uuid);

		if (applied != null) {
			return of(applied);
		} else if (uuid.equals(PlayerProfile.STEVE.profile().id())) {
			return DEFAULT_WIDE_SKINS[0];
		} else if (uuid.equals(PlayerProfile.ALEX.profile().id())) {
			return DEFAULT_SLIM_SKINS[1];
		} else {
			var profile = PlayerProfiles.get(uuid);
			var modelType = PlayerSkins.getModelType(profile);
			var skin = PlayerSkins.get(mc, uuid);
			skin.load(mc, blocking);
			// Gallery image ids ARE full texture paths (e.g. vidlib:textures/vidlib/generated/gallery/...png),
			// so the texture path must be used verbatim - the single-arg ResourceTexture ctor would
			// prepend "textures/" and append ".png" again, producing an unloadable id.
			var body = new net.minecraft.core.ClientAsset.ResourceTexture(skin.textureId(), skin.textureId());
			dev.latvian.mods.vidlib.VidLib.LOGGER.info("[skindbg] {} id={} body.texturePath={}", uuid, skin.textureId(), body.texturePath());
			return PlayerSkin.insecure(body, null, null, modelType);
		}
	}

	@Nullable
	static SkinTexture getAppliedSkin(Minecraft mc, UUID uuid) {
		var level = mc.level;

		if (level != null) {
			var player = level.getPlayerByUUID(uuid);

			if (player instanceof AbstractClientPlayer clientPlayer) {
				return ClientGameEngine.INSTANCE.overrideSkin(clientPlayer);
			}
		}

		return null;
	}

	@Nullable
	static PlayerSkin getLiveSkin(@Nullable Entity entity) {
		if (entity instanceof AbstractClientPlayer player) {
			return player.getSkin();
		}

		return null;
	}
}
