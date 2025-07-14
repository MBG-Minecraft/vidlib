package dev.latvian.mods.vidlib.feature.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.util.StringUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class Stage {
	public static final List<Stage> LIST = new ArrayList<>();

	public static void add(Stage stage) {
		stage.index = LIST.size();
		LIST.add(stage);
	}

	public static final Codec<Stage> CODEC = Codec.STRING.flatXmap(s -> {
		for (var stage : LIST) {
			if (stage.id.equals(s)) {
				return DataResult.success(stage);
			}
		}

		return DataResult.error(() -> "Unknown stage: " + s);
	}, stage -> DataResult.success(stage.id));

	public static final StreamCodec<ByteBuf, Stage> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(LIST::get, stage -> stage.index);

	public static final DataType<Stage> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Stage.class);

	public final String id;
	public String displayName;
	public ImIcon icon;
	public int index;
	public final List<Scene> scenes;

	public Stage(String id) {
		this.id = id;
		this.displayName = StringUtils.snakeCaseToTitleCase(id);
		this.icon = ImIcon.NONE;
		this.index = -1;
		this.scenes = new ArrayList<>(1);
	}

	public void addScene(Scene scene) {
		scene.stage = this;
		scene.index = scenes.size();
		scenes.add(scene);
	}

	public void prepare(ServerLevel level) {
	}

	public void start(ServerLevel level) {
	}
}
