package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.util.Hex64;

import java.util.ArrayList;
import java.util.List;

public record HubUserFlags(
	Hex64 flags,
	boolean hubAdmin,
	boolean hubStaff,
	boolean bot,
	boolean externalTalent,
	boolean internalTalent,
	boolean developer,
	boolean videoEditor,
	boolean tester,
	boolean deleted,
	boolean nda,
	boolean synthetic
) {
	public static final long HUB_ADMIN = 1L << 0L;
	public static final long HUB_STAFF = 1L << 1L;
	public static final long BOT = 1L << 2L;
	public static final long EXTERNAL_TALENT = 1L << 3L;
	public static final long INTERNAL_TALENT = 1L << 4L;
	public static final long DEVELOPER = 1L << 5L;
	public static final long VIDEO_EDITOR = 1L << 6L;
	public static final long TESTER = 1L << 7L;
	public static final long DELETED = 1L << 8L;
	public static final long NDA = 1L << 9L;
	public static final long SYNTHETIC = 1L << 10L;

	public static final HubUserFlags NONE = new HubUserFlags(Hex64.NONE);

	public static final Codec<HubUserFlags> CODEC = Hex64.LENIENT_CODEC.xmap(HubUserFlags::new, HubUserFlags::flags);

	public HubUserFlags(Hex64 flags) {
		this(
			flags,
			flags.is(HUB_ADMIN),
			flags.is(HUB_STAFF),
			flags.is(BOT),
			flags.is(EXTERNAL_TALENT),
			flags.is(INTERNAL_TALENT),
			flags.is(DEVELOPER),
			flags.is(VIDEO_EDITOR),
			flags.is(TESTER),
			flags.is(DELETED),
			flags.is(NDA),
			flags.is(SYNTHETIC)
		);
	}

	public boolean isHubStaff() {
		return hubAdmin || hubStaff;
	}

	public boolean isStaff() {
		return isHubStaff() || developer || videoEditor;
	}

	public boolean isTalent() {
		return externalTalent || internalTalent;
	}

	public boolean isTesterWithNDA() {
		return tester && nda;
	}

	public List<String> getRoles() {
		var list = new ArrayList<String>(1);

		if (hubAdmin) {
			list.add("Hub Admin");
		} else if (hubStaff) {
			list.add("Hub Staff");
		} else if (isStaff()) {
			list.add("Staff");
		}

		if (isTalent()) {
			list.add("Talent");
		}

		if (developer) {
			list.add("Developer");
		}

		if (videoEditor) {
			list.add("Video Editor");
		}

		if (tester) {
			list.add("Tester");
		}

		if (nda) {
			list.add("NDA");
		}

		if (list.isEmpty()) {
			list.add("Contestant");
		}

		return list;
	}
}
