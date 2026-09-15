package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record HubUserFlags(
	boolean deleted,
	boolean bot,
	boolean hubAdmin,
	boolean hubStaff,
	boolean externalTalent,
	boolean internalTalent,
	boolean developer,
	boolean videoEditor,
	boolean tester,
	boolean nda
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
		false,
		false
	);

	public static final Codec<HubUserFlags> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("deleted", false).forGetter(HubUserFlags::deleted),
		Codec.BOOL.optionalFieldOf("bot", false).forGetter(HubUserFlags::bot),
		Codec.BOOL.optionalFieldOf("hub_admin", false).forGetter(HubUserFlags::hubAdmin),
		Codec.BOOL.optionalFieldOf("hub_staff", false).forGetter(HubUserFlags::hubStaff),
		Codec.BOOL.optionalFieldOf("external_talent", false).forGetter(HubUserFlags::externalTalent),
		Codec.BOOL.optionalFieldOf("internal_talent", false).forGetter(HubUserFlags::internalTalent),
		Codec.BOOL.optionalFieldOf("developer", false).forGetter(HubUserFlags::developer),
		Codec.BOOL.optionalFieldOf("video_editor", false).forGetter(HubUserFlags::videoEditor),
		Codec.BOOL.optionalFieldOf("tester", false).forGetter(HubUserFlags::tester),
		Codec.BOOL.optionalFieldOf("nda", false).forGetter(HubUserFlags::nda)
	).apply(instance, HubUserFlags::new));

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
