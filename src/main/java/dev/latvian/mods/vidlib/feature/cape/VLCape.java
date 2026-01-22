package dev.latvian.mods.vidlib.feature.cape;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

@AutoInit
public record VLCape(ResourceLocation capeTexture) {
	public static final Codec<VLCape> CODEC = ResourceLocation.CODEC.xmap(VLCape::new, VLCape::capeTexture);

	public static final StreamCodec<ByteBuf, VLCape> STREAM_CODEC = CompositeStreamCodec.of(ResourceLocation.STREAM_CODEC, VLCape::capeTexture, VLCape::new);

	public static final DataType<VLCape> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, VLCape.class);

	public static PlayerSkin addCapeToSkin(AbstractClientPlayer player, PlayerSkin oldSkin) {
		VLCape cape = player.getOptional(InternalPlayerData.CAPE);

		if (cape == null) {
			return oldSkin;
		}

		return new PlayerSkin(
			oldSkin.texture(),
			oldSkin.textureUrl(),
			cape.capeTexture(),
			oldSkin.elytraTexture(),
			oldSkin.model(),
			oldSkin.secure()
		);
	}

}
