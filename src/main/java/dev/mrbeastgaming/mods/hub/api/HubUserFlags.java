package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HubUserFlags(
	boolean deleted,
	boolean bot,
	boolean admin,
	boolean miscStaff,
	boolean externalTalent,
	boolean internalTalent,
	boolean developer,
	boolean videoEditor,
	boolean testerWithNDA
) {
	public static final HubUserFlags EMPTY = new HubUserFlags(
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false
	);

	public static final Codec<HubUserFlags> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("deleted", false).forGetter(HubUserFlags::deleted),
		Codec.BOOL.optionalFieldOf("bot", false).forGetter(HubUserFlags::bot),
		Codec.BOOL.optionalFieldOf("admin", false).forGetter(HubUserFlags::admin),
		Codec.BOOL.optionalFieldOf("misc_staff", false).forGetter(HubUserFlags::miscStaff),
		Codec.BOOL.optionalFieldOf("external_talent", false).forGetter(HubUserFlags::externalTalent),
		Codec.BOOL.optionalFieldOf("internal_talent", false).forGetter(HubUserFlags::internalTalent),
		Codec.BOOL.optionalFieldOf("developer", false).forGetter(HubUserFlags::developer),
		Codec.BOOL.optionalFieldOf("video_editor", false).forGetter(HubUserFlags::videoEditor),
		Codec.BOOL.optionalFieldOf("tester_with_nda", false).forGetter(HubUserFlags::testerWithNDA)
	).apply(instance, HubUserFlags::new));

	public boolean isStaff() {
		return admin || miscStaff;
	}

	public boolean isAnyStaff() {
		return isStaff() || developer || videoEditor;
	}

	public boolean isTalent() {
		return externalTalent || internalTalent;
	}
}
