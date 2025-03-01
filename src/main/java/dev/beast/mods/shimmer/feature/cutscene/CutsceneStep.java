package dev.beast.mods.shimmer.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumber;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class CutsceneStep {
	public static final int ORIGIN = 1 << 0;
	public static final int TARGET = 1 << 1;
	public static final int SNAP_ORIGIN = 1 << 2;
	public static final int SNAP_TARGET = 1 << 3;
	public static final int STATUS = 1 << 4;
	public static final int TOP_BAR = 1 << 5;
	public static final int BOTTOM_BAR = 1 << 6;
	public static final int SHADER = 1 << 7;
	public static final int ZOOM = 1 << 8;
	public static final int SNAP_ZOOM = 1 << 9;
	public static final int NO_SCREEN = 1 << 10;

	public static final Codec<CutsceneStep> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("start", 0).forGetter(step -> step.start),
		Codec.INT.fieldOf("length").forGetter(step -> step.length)
	).apply(instance, CutsceneStep::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneStep> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public CutsceneStep decode(RegistryFriendlyByteBuf buf) {
			var step = new CutsceneStep(buf.readVarInt(), buf.readVarInt());
			step.flags = buf.readVarInt();
			step.origin = ((step.flags & ORIGIN) != 0) ? WorldPosition.STREAM_CODEC.decode(buf) : null;
			step.target = ((step.flags & TARGET) != 0) ? WorldPosition.STREAM_CODEC.decode(buf) : null;
			step.zoom = ((step.flags & ZOOM) != 0) ? WorldNumber.STREAM_CODEC.decode(buf) : null;
			step.status = ((step.flags & STATUS) != 0) ? ComponentSerialization.STREAM_CODEC.decode(buf) : null;
			step.topBar = ((step.flags & TOP_BAR) != 0) ? ComponentSerialization.STREAM_CODEC.decode(buf) : null;
			step.bottomBar = ((step.flags & BOTTOM_BAR) != 0) ? ComponentSerialization.STREAM_CODEC.decode(buf) : null;

			if ((step.flags & SHADER) != 0) {
				var s = buf.readUtf();
				step.shader = s.isEmpty() ? null : ResourceLocation.parse(s);
			} else {
				step.shader = null;
			}

			return step;
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, CutsceneStep step) {
			buf.writeVarInt(step.start);
			buf.writeVarInt(step.length);
			buf.writeVarInt(step.flags);

			if ((step.flags & ORIGIN) != 0) {
				WorldPosition.STREAM_CODEC.encode(buf, step.origin);
			}

			if ((step.flags & TARGET) != 0) {
				WorldPosition.STREAM_CODEC.encode(buf, step.target);
			}

			if ((step.flags & ZOOM) != 0) {
				WorldNumber.STREAM_CODEC.encode(buf, step.zoom);
			}

			if ((step.flags & STATUS) != 0) {
				ComponentSerialization.STREAM_CODEC.encode(buf, step.status);
			}

			if ((step.flags & TOP_BAR) != 0) {
				ComponentSerialization.STREAM_CODEC.encode(buf, step.topBar);
			}

			if ((step.flags & BOTTOM_BAR) != 0) {
				ComponentSerialization.STREAM_CODEC.encode(buf, step.bottomBar);
			}

			if ((step.flags & SHADER) != 0) {
				var s = step.shader == null ? "" : step.shader.toString();
				buf.writeUtf(s);
			}
		}
	};

	public static CutsceneStep create(int start, int length) {
		return new CutsceneStep(start, Math.max(1, length));
	}

	public static CutsceneStep first(int length) {
		return create(0, length);
	}

	public static CutsceneStep at(int start) {
		return create(start, 1);
	}

	public final int start;
	public final int length;
	public int flags;
	public WorldPosition origin;
	public WorldPosition target;
	public WorldNumber zoom;
	public Component status;
	public Component topBar;
	public Component bottomBar;
	public ResourceLocation shader;
	public List<CutsceneTick> tick;

	public Vec3 prevRenderTarget, renderTarget;

	@OnlyIn(Dist.CLIENT)
	public List<CutsceneRender> render;

	private CutsceneStep(int start, int length) {
		this.start = start;
		this.length = length;
	}

	public CutsceneStep origin(WorldPosition origin) {
		this.origin = origin;
		this.flags |= ORIGIN;
		return this;
	}

	public CutsceneStep target(WorldPosition target) {
		this.target = target;
		this.flags |= TARGET;
		return this;
	}

	public CutsceneStep rotated(WorldPosition origin, double yaw, double pitch) {
		return origin(origin).target(origin.offset(WorldPosition.ofRotation(yaw, pitch)));
	}

	public CutsceneStep zoom(WorldNumber zoom) {
		this.zoom = zoom;
		this.flags |= ZOOM;
		return this;
	}

	public CutsceneStep snapOrigin() {
		this.flags |= SNAP_ORIGIN;
		return this;
	}

	public CutsceneStep snapTarget() {
		this.flags |= SNAP_TARGET;
		return this;
	}

	public CutsceneStep snapZoom() {
		this.flags |= SNAP_ZOOM;
		return this;
	}

	public CutsceneStep snap() {
		return snapOrigin().snapTarget().snapZoom();
	}

	public CutsceneStep status(Component status) {
		this.status = status;
		this.flags |= STATUS;
		return this;
	}

	public CutsceneStep shader(ResourceLocation shader) {
		this.shader = shader;
		this.flags |= SHADER;
		return this;
	}

	public CutsceneStep shader(String name) {
		return shader(ResourceLocation.withDefaultNamespace("shaders/post/" + name + ".json"));
	}

	public CutsceneStep topBar(Component topBar) {
		this.topBar = topBar;
		this.flags |= TOP_BAR;
		return this;
	}

	public CutsceneStep bottomBar(Component bottomBar) {
		this.bottomBar = bottomBar;
		this.flags |= BOTTOM_BAR;
		return this;
	}

	public CutsceneStep tick(CutsceneTick tick) {
		if (this.tick == null) {
			this.tick = new ArrayList<>(1);
		}

		this.tick.add(tick);
		return this;
	}

	public CutsceneStep noScreen(boolean noScreen) {
		if (noScreen) {
			this.flags |= NO_SCREEN;
		}

		return this;
	}

	@OnlyIn(Dist.CLIENT)
	public CutsceneStep render(CutsceneRender render) {
		if (this.render == null) {
			this.render = new ArrayList<>(1);
		}

		this.render.add(render);
		return this;
	}
}
