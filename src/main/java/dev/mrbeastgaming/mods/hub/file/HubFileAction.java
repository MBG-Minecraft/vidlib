package dev.mrbeastgaming.mods.hub.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import dev.mrbeastgaming.mods.hub.api.HubFileType;

import java.util.List;

public sealed interface HubFileAction {
	Codec<HubFileAction> CODEC = Codec.STRING.dispatch("action", HubFileAction::action, s -> switch (s) {
		case "finish" -> Finish.MAP_CODEC;
		case "archive" -> Archive.MAP_CODEC;
		case "join" -> Join.MAP_CODEC;
		case "add_to_project" -> AddToProject.MAP_CODEC;
		default -> throw new IllegalStateException("Unknown Action: " + s);
	});

	Codec<List<HubFileAction>> LIST_CODEC = CODEC.listOf();

	String action();

	enum Finish implements HubFileAction {
		INSTANCE;

		public static final MapCodec<Finish> MAP_CODEC = MapCodec.unit(INSTANCE);

		@Override
		public String action() {
			return "finish";
		}
	}

	record AddToProject(
		String path,
		HubFileType fileType,
		Checksum uniqueId,
		PossibleUser assignTo
	) implements HubFileAction {
		public static final MapCodec<AddToProject> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("path").forGetter(AddToProject::path),
			HubFileType.CODEC.optionalFieldOf("file_type", HubFileType.UNKNOWN).forGetter(AddToProject::fileType),
			Checksum.CODEC.optionalFieldOf("unique_id", NoChecksum.INSTANCE).forGetter(AddToProject::uniqueId),
			PossibleUser.CODEC.optionalFieldOf("assign_to", PossibleUser.NONE).forGetter(AddToProject::assignTo)
		).apply(i, AddToProject::new));

		@Override
		public String action() {
			return "add_to_project";
		}
	}

	record Archive(
		String file,
		List<FromTo> contents
	) implements HubFileAction {
		public static final MapCodec<Archive> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("file").forGetter(Archive::file),
			FromTo.CODEC.listOf().fieldOf("contents").forGetter(Archive::contents)
		).apply(i, Archive::new));

		@Override
		public String action() {
			return "archive";
		}
	}

	record FromTo(String from, String to) {
		public static final MapCodec<FromTo> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("from").forGetter(FromTo::from),
			Codec.STRING.fieldOf("to").forGetter(FromTo::to)
		).apply(i, FromTo::new));

		public static final Codec<FromTo> CODEC = MAP_CODEC.codec();

		public FromTo(String both) {
			this(both, both);
		}
	}

	record Join(
		String file,
		List<String> parts
	) implements HubFileAction {
		public static final MapCodec<Join> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("file").forGetter(Join::file),
			Codec.STRING.listOf().fieldOf("parts").forGetter(Join::parts)
		).apply(i, Join::new));

		@Override
		public String action() {
			return "join";
		}
	}
}
