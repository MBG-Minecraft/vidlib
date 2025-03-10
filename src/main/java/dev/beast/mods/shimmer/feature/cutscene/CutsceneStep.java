package dev.beast.mods.shimmer.feature.cutscene;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.cutscene.event.CutsceneEvent;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumber;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CutsceneStep {
	public record Snap(boolean origin, boolean target, boolean zoom) {
		public static final Snap NONE = new Snap(false);
		public static final Snap ALL = new Snap(true);

		public static final Codec<Snap> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("origin", false).forGetter(Snap::origin),
			Codec.BOOL.optionalFieldOf("target", false).forGetter(Snap::target),
			Codec.BOOL.optionalFieldOf("zoom", false).forGetter(Snap::zoom)
		).apply(instance, Snap::new));

		public static final StreamCodec<ByteBuf, Snap> STREAM_CODEC = new StreamCodec<>() {
			@Override
			public Snap decode(ByteBuf buf) {
				int flags = buf.readByte() & 0xFF;
				return flags == 0 ? NONE : new Snap(
					(flags & 1) != 0,
					(flags & 2) != 0,
					(flags & 4) != 0
				);
			}

			@Override
			public void encode(ByteBuf buf, Snap value) {
				buf.writeByte((value.origin() ? 1 : 0) | (value.target() ? 2 : 0) | (value.zoom() ? 4 : 0));
			}
		};

		public static final Codec<Snap> CODEC = Codec.either(Codec.BOOL, OBJECT_CODEC).xmap(either -> either.map(v -> v ? ALL : NONE, Function.identity()), Either::right);

		private Snap(boolean value) {
			this(value, value, value);
		}
	}

	public static final Codec<CutsceneStep> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("start", 0).forGetter(s -> s.start),
		Codec.INT.fieldOf("length").forGetter(s -> s.length),
		WorldPosition.CODEC.optionalFieldOf("origin").forGetter(s -> s.origin),
		WorldPosition.CODEC.optionalFieldOf("target").forGetter(s -> s.target),
		WorldNumber.CODEC.optionalFieldOf("zoom").forGetter(s -> s.zoom),
		ComponentSerialization.CODEC.optionalFieldOf("status").forGetter(s -> s.status),
		ComponentSerialization.CODEC.optionalFieldOf("top_bar").forGetter(s -> s.topBar),
		ComponentSerialization.CODEC.optionalFieldOf("bottom_bar").forGetter(s -> s.bottomBar),
		ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(s -> s.shader),
		Snap.CODEC.optionalFieldOf("snap", Snap.NONE).forGetter(s -> s.snap),
		CutsceneEvent.CODEC.listOf().optionalFieldOf("events", List.of()).forGetter(s -> s.events)
	).apply(instance, CutsceneStep::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, s -> s.start,
		ByteBufCodecs.VAR_INT, s -> s.length,
		WorldPosition.STREAM_CODEC.optional(), s -> s.origin,
		WorldPosition.STREAM_CODEC.optional(), s -> s.target,
		WorldNumber.STREAM_CODEC.optional(), s -> s.zoom,
		ComponentSerialization.STREAM_CODEC.optional(), s -> s.status,
		ComponentSerialization.STREAM_CODEC.optional(), s -> s.topBar,
		ComponentSerialization.STREAM_CODEC.optional(), s -> s.bottomBar,
		ResourceLocation.STREAM_CODEC.optional(), s -> s.shader,
		Snap.STREAM_CODEC, s -> s.snap,
		CutsceneEvent.REGISTRY.valueStreamCodec().list(), s -> s.events,
		CutsceneStep::new
	);

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
	public Optional<WorldPosition> origin = Optional.empty();
	public Optional<WorldPosition> target = Optional.empty();
	public Optional<WorldNumber> zoom = Optional.empty();
	public Optional<Component> status = Optional.empty();
	public Optional<Component> topBar = Optional.empty();
	public Optional<Component> bottomBar = Optional.empty();
	public Optional<ResourceLocation> shader = Optional.empty();
	public Snap snap = Snap.NONE;
	public List<CutsceneEvent> events = List.of();

	private CutsceneStep(
		int start,
		int length,
		Optional<WorldPosition> origin,
		Optional<WorldPosition> target,
		Optional<WorldNumber> zoom,
		Optional<Component> status,
		Optional<Component> topBar,
		Optional<Component> bottomBar,
		Optional<ResourceLocation> shader,
		Snap snap,
		List<CutsceneEvent> events
	) {
		this.start = start;
		this.length = length;
		this.origin = origin;
		this.target = target;
		this.zoom = zoom;
		this.status = status;
		this.topBar = topBar;
		this.bottomBar = bottomBar;
		this.shader = shader;
		this.snap = snap;
	}

	public Vec3 prevRenderTarget, renderTarget;

	@OnlyIn(Dist.CLIENT)
	public List<CutsceneRender> render;

	private CutsceneStep(int start, int length) {
		this.start = start;
		this.length = length;
	}

	public CutsceneStep origin(WorldPosition origin) {
		this.origin = Optional.of(origin);
		return this;
	}

	public CutsceneStep target(WorldPosition target) {
		this.target = Optional.of(target);
		return this;
	}

	public CutsceneStep rotated(WorldPosition origin, double yaw, double pitch) {
		return origin(origin).target(origin.offset(WorldPosition.ofRotation(yaw, pitch)));
	}

	public CutsceneStep zoom(WorldNumber zoom) {
		this.zoom = Optional.of(zoom);
		return this;
	}

	public CutsceneStep snapOrigin() {
		this.snap = new Snap(true, snap.target, snap.zoom);
		return this;
	}

	public CutsceneStep snapTarget() {
		this.snap = new Snap(snap.origin, true, snap.zoom);
		return this;
	}

	public CutsceneStep snapZoom() {
		this.snap = new Snap(snap.origin, snap.target, true);
		return this;
	}

	public CutsceneStep snap() {
		this.snap = Snap.ALL;
		return this;
	}

	public CutsceneStep status(Component status) {
		this.status = Optional.of(status);
		return this;
	}

	public CutsceneStep shader(ResourceLocation shader) {
		this.shader = Optional.of(shader);
		return this;
	}

	public CutsceneStep shader(String name) {
		return shader(ResourceLocation.withDefaultNamespace("shaders/post/" + name + ".json"));
	}

	public CutsceneStep topBar(Component topBar) {
		this.topBar = Optional.of(topBar);
		return this;
	}

	public CutsceneStep bottomBar(Component bottomBar) {
		this.bottomBar = Optional.of(bottomBar);
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
