package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.ID;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record SkyboxTextureId(
	Identifier id,
	ClientAsset.ResourceTexture source,
	ClientAsset.ResourceTexture generated
) {
	public static final Codec<SkyboxTextureId> CODEC = ID.CODEC.xmap(SkyboxTextureId::new, SkyboxTextureId::id);
	public static final StreamCodec<ByteBuf, SkyboxTextureId> STREAM_CODEC = ID.STREAM_CODEC.map(SkyboxTextureId::new, SkyboxTextureId::id);
	public static final DataType<SkyboxTextureId> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC);

	public static final SkyboxTextureId NONE = new SkyboxTextureId(ID.mc("none"));
	public static final SkyboxTextureId TEMPLATE = new SkyboxTextureId(ID.vidlib("template"));

	public SkyboxTextureId(Identifier id) {
		this(
			id,
			new ClientAsset.ResourceTexture(id.withPath("vidlib/skybox/" + id.getPath())),
			new ClientAsset.ResourceTexture(id.withPath("vidlib/generated/skybox/" + id.getPath()))
		);
	}
}
