package dev.latvian.mods.vidlib.feature.note;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.util.Timestamp;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.replay.api.ReplayMarkerGroup;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record Note(UUID id, NameAndId from, String text, ResourceKey<Level> dimension, Optional<Vec3> position, Timestamp timestamp, NoteVisibility visibility) {
	public static List<Note> REPLAY_NOTES = null;

	public static final StreamCodec<ByteBuf, Note> STREAM_CODEC = CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, Note::id,
		MCStreamCodecs.NAME_AND_ID, Note::from,
		ByteBufCodecs.STRING_UTF8, Note::text,
		MCStreamCodecs.DIMENSION, Note::dimension,
		ByteBufCodecs.optional(MCStreamCodecs.VEC3), Note::position,
		Timestamp.STREAM_CODEC, Note::timestamp,
		NoteVisibility.STREAM_CODEC, Note::visibility,
		Note::new
	);

	public Note(Player player, String text, NoteVisibility visibility) {
		this(UUID.randomUUID(), player.nameAndId(), text, player.level().dimension(), Optional.of(player.getEyePosition()), Timestamp.now(player.level().getGameTime()), visibility);
	}

	public Note withText(String text) {
		return new Note(id, from, text, dimension, position, timestamp, visibility);
	}

	public Note withVisibility(NoteVisibility visibility) {
		return new Note(id, from, text, dimension, position, timestamp, visibility);
	}

	public ReplayMarkerData toMarkerData() {
		return ReplayMarkerData.builder()
			.group(visibility == NoteVisibility.PRIVATE ? ReplayMarkerGroup.PRIVATE_NOTES : ReplayMarkerGroup.PUBLIC_NOTES)
			.customData("note", new LongArrayTag(new long[]{id.getMostSignificantBits(), id.getLeastSignificantBits()}))
			.dimension(dimension)
			.position(position)
			.description(text)
			.build();
	}
}
