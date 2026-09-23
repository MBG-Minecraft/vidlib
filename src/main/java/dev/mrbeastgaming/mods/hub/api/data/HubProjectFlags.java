package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.util.Hex64;

public record HubProjectFlags(
	Hex64 flags,
	boolean visible,
	boolean archived,
	boolean shortForm,
	boolean basePack,
	boolean drive
) {
	public static final HubProjectFlags NONE = new HubProjectFlags(Hex64.NONE);

	public static final long VISIBLE = 1L << 0L;
	public static final long ARCHIVED = 1L << 7L;
	public static final long SHORT_FORM = 1L << 9L;
	public static final long BASE_PACK = 1L << 10L;
	public static final long DRIVE = 1L << 11L;

	public static final Codec<HubProjectFlags> CODEC = Hex64.LENIENT_CODEC.xmap(HubProjectFlags::new, HubProjectFlags::flags);

	public HubProjectFlags(Hex64 flags) {
		this(
			flags,
			flags.is(VISIBLE),
			flags.is(ARCHIVED),
			flags.is(SHORT_FORM),
			flags.is(BASE_PACK),
			flags.is(DRIVE)
		);
	}
}
