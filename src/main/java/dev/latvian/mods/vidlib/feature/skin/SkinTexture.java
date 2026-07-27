package dev.latvian.mods.vidlib.feature.skin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.ID;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Set;

public record SkinTexture(ClientAsset.ResourceTexture asset, boolean slim) {
	public static final SkinTexture WIDE_STEVE = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/steve")), false);
	public static final SkinTexture WIDE_ALEX = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/alex")), false);
	public static final SkinTexture WIDE_ARI = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/ari")), false);
	public static final SkinTexture WIDE_EFE = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/efe")), false);
	public static final SkinTexture WIDE_KAI = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/kai")), false);
	public static final SkinTexture WIDE_MAKENA = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/makena")), false);
	public static final SkinTexture WIDE_NOOR = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/noor")), false);
	public static final SkinTexture WIDE_SUNNY = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/sunny")), false);
	public static final SkinTexture WIDE_ZURI = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/zuri")), false);

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

	public static final SkinTexture SLIM_STEVE = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/slim/steve")), true);
	public static final SkinTexture SLIM_ALEX = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/slim/alex")), true);
	public static final SkinTexture SLIM_ARI = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/slim/ari")), true);
	public static final SkinTexture SLIM_EFE = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/slim/efe")), true);
	public static final SkinTexture SLIM_KAI = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/slim/kai")), true);
	public static final SkinTexture SLIM_MAKENA = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/slim/makena")), true);
	public static final SkinTexture SLIM_NOOR = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/slim/noor")), true);
	public static final SkinTexture SLIM_SUNNY = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/slim/sunny")), true);
	public static final SkinTexture SLIM_ZURI = new SkinTexture(new ClientAsset.ResourceTexture(ID.mc("entity/player/slim/zuri")), true);

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
		ClientAsset.ResourceTexture.CODEC.fieldOf("texture").forGetter(SkinTexture::asset),
		Codec.BOOL.optionalFieldOf("slim", false).forGetter(SkinTexture::slim)
	).apply(instance, SkinTexture::new));

	public static final StreamCodec<ByteBuf, SkinTexture> STREAM_CODEC = CompositeStreamCodec.of(
		ClientAsset.ResourceTexture.STREAM_CODEC, SkinTexture::asset,
		ByteBufCodecs.BOOL, SkinTexture::slim,
		SkinTexture::new
	);

	public static final DataType<SkinTexture> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC);
	public static final DataType<List<SkinTexture>> LIST_DATA_TYPE = DATA_TYPE.listOf();
	public static final DataType<Set<SkinTexture>> SET_DATA_TYPE = DATA_TYPE.setOf();
}
