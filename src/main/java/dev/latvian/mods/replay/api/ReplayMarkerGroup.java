package dev.latvian.mods.replay.api;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ReplayMarkerGroup {
	public static final Map<String, ReplayMarkerGroup> GROUPS = new HashMap<>();

	public static ReplayMarkerGroup get(String id) {
		return GROUPS.computeIfAbsent(id, ReplayMarkerGroup::new);
	}

	public static ReplayMarkerGroup make(String id, Consumer<ReplayMarkerGroup> callback) {
		var group = get(id);
		callback.accept(group);
		return group;
	}

	public static final Codec<ReplayMarkerGroup> CODEC = Codec.STRING.xmap(ReplayMarkerGroup::get, g -> g.id);
	public static final StreamCodec<ByteBuf, ReplayMarkerGroup> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ReplayMarkerGroup::get, g -> g.id);

	public static final ReplayMarkerGroup DEFAULT = make("default", group -> {
		group.displayName = Component.literal("Default");
		group.defaultColor = Color.WHITE;
	});

	public static final ReplayMarkerGroup DATA_SYNC = make("data_sync", group -> {
		group.displayName = Component.literal("Data Sync");
		group.defaultColor = Color.MAGENTA;
		group.visible = false;
		group.renderInWorld = false;
	});

	public static final ReplayMarkerGroup PLAYER_ADDED = make("player_added", group -> {
		group.displayName = Component.literal("Player Added");
		group.defaultColor = Color.GREEN;
		group.visible = false;
		group.renderInWorld = false;
	});

	public static final ReplayMarkerGroup PLAYER_REMOVED = make("player_removed", group -> {
		group.displayName = Component.literal("Player Removed");
		group.defaultColor = Color.RED;
		group.visible = false;
		group.renderInWorld = false;
	});

	public static final ReplayMarkerGroup CHANGED_DIMENSION = make("changed_dimension", group -> {
		group.displayName = Component.literal("Changed Dimension");
		group.defaultColor = Color.ofRGB(0xAA00AA);
		group.visible = false;
		group.renderInWorld = false;
	});

	public static final ReplayMarkerGroup PUBLIC_NOTES = make("public_notes", group -> {
		group.displayName = Component.literal("Public Notes");
		group.defaultColor = Color.ofRGB(0xFFDB99);
		group.renderInWorld = false;
	});

	public static final ReplayMarkerGroup PRIVATE_NOTES = make("private_notes", group -> {
		group.displayName = Component.literal("Private Notes");
		group.defaultColor = Color.ofRGB(0xFFDB99);
		group.renderInWorld = false;
	});

	@AutoInit
	public static void bootstrap() {
	}

	public final String id;
	public Component displayName;
	public Color defaultColor;
	public boolean visible;
	public boolean renderInWorld;

	private ReplayMarkerGroup(String id) {
		this.id = id;
		this.displayName = null;
		this.defaultColor = Color.WHITE;
		this.visible = true;
		this.renderInWorld = true;
	}

	public String getDisplayName() {
		return displayName == null ? id : displayName.getString();
	}
}
