package dev.latvian.mods.vidlib.feature.skin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.ID;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record SkinTexture(ResourceLocation texture, boolean slim) {
	public static final ResourceLocation STEVE = ID.mc("textures/entity/player/wide/steve.png");
	public static final ResourceLocation ALEX = ID.mc("textures/entity/player/wide/alex.png");
	public static final ResourceLocation ARI = ID.mc("textures/entity/player/wide/ari.png");
	public static final ResourceLocation EFE = ID.mc("textures/entity/player/wide/efe.png");
	public static final ResourceLocation KAI = ID.mc("textures/entity/player/wide/kai.png");
	public static final ResourceLocation MAKENA = ID.mc("textures/entity/player/wide/makena.png");
	public static final ResourceLocation NOOR = ID.mc("textures/entity/player/wide/noor.png");
	public static final ResourceLocation SUNNY = ID.mc("textures/entity/player/wide/sunny.png");
	public static final ResourceLocation ZURI = ID.mc("textures/entity/player/wide/zuri.png");

	public static final SkinTexture WIDE_STEVE = new SkinTexture(STEVE, false);
	public static final SkinTexture WIDE_ALEX = new SkinTexture(ALEX, false);
	public static final SkinTexture WIDE_ARI = new SkinTexture(ARI, false);
	public static final SkinTexture WIDE_EFE = new SkinTexture(EFE, false);
	public static final SkinTexture WIDE_KAI = new SkinTexture(KAI, false);
	public static final SkinTexture WIDE_MAKENA = new SkinTexture(MAKENA, false);
	public static final SkinTexture WIDE_NOOR = new SkinTexture(NOOR, false);
	public static final SkinTexture WIDE_SUNNY = new SkinTexture(SUNNY, false);
	public static final SkinTexture WIDE_ZURI = new SkinTexture(ZURI, false);

	public static final SkinTexture[] DEFAULT_WIDE = {
		WIDE_STEVE,
		WIDE_ALEX,
		WIDE_ARI,
		WIDE_EFE,
		WIDE_KAI,
		WIDE_MAKENA,
		WIDE_NOOR,
		WIDE_SUNNY,
		WIDE_ZURI
	};

	public static final SkinTexture SLIM_STEVE = new SkinTexture(STEVE, true);
	public static final SkinTexture SLIM_ALEX = new SkinTexture(ALEX, true);
	public static final SkinTexture SLIM_ARI = new SkinTexture(ARI, true);
	public static final SkinTexture SLIM_EFE = new SkinTexture(EFE, true);
	public static final SkinTexture SLIM_KAI = new SkinTexture(KAI, true);
	public static final SkinTexture SLIM_MAKENA = new SkinTexture(MAKENA, true);
	public static final SkinTexture SLIM_NOOR = new SkinTexture(NOOR, true);
	public static final SkinTexture SLIM_SUNNY = new SkinTexture(SUNNY, true);
	public static final SkinTexture SLIM_ZURI = new SkinTexture(ZURI, true);

	public static final SkinTexture[] DEFAULT_SLIM = {
		SLIM_STEVE,
		SLIM_ALEX,
		SLIM_ARI,
		SLIM_EFE,
		SLIM_KAI,
		SLIM_MAKENA,
		SLIM_NOOR,
		SLIM_SUNNY,
		SLIM_ZURI
	};

	public static final Codec<SkinTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.fieldOf("texture").forGetter(SkinTexture::texture),
		Codec.BOOL.optionalFieldOf("slim", false).forGetter(SkinTexture::slim)
	).apply(instance, SkinTexture::new));

	public static final StreamCodec<ByteBuf, SkinTexture> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, SkinTexture::texture,
		ByteBufCodecs.BOOL, SkinTexture::slim,
		SkinTexture::new
	);

	public static final DataType<SkinTexture> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, SkinTexture.class);
}
