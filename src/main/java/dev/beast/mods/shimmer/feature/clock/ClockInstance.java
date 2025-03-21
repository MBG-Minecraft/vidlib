package dev.beast.mods.shimmer.feature.clock;

import com.mojang.brigadier.context.CommandContext;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.util.registry.RegistryReference;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ClockInstance {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClockInstance> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
		Clock.KNOWN_CODEC.streamCodec(),
		i -> i.clock,
		ByteBufCodecs.VAR_INT,
		i -> i.tick,
		ByteBufCodecs.BOOL,
		i -> i.ticking,
		ClockInstance::new
	);

	public static final RegistryReference.IdHolder<ClockInstance> REGISTRY = RegistryReference.createServerIdHolder("clock_instance", false);
	public static final KnownCodec<ClockInstance> KNOWN_CODEC = KnownCodec.of(REGISTRY, ClockInstance.class);

	public static int command(CommandContext<CommandSourceStack> ctx, BiConsumer<ClockInstance, MinecraftServer> callback) {
		var instance = KNOWN_CODEC.get(ctx, "id");

		if (instance != null) {
			callback.accept(instance, ctx.getSource().getServer());
			return 1;
		}

		return 0;
	}

	public final Clock clock;
	public int prevTick;
	public int tick;
	public boolean ticking;
	private Int2ObjectMap<List<String>> events;
	private List<String> allEvents;

	public ClockInstance(Clock clock, int tick, boolean ticking) {
		this.clock = clock;
		this.prevTick = tick;
		this.tick = tick;
		this.ticking = ticking;
	}

	public void tick(Level level) {
		prevTick = tick;

		if (ticking && tick < clock.maxTicks()) {
			tick++;

			if (tick > clock.maxTicks()) {
				tick = clock.maxTicks();
				ticking = false;
			}

			if (events == null) {
				events = new Int2ObjectOpenHashMap<>();
				allEvents = new ArrayList<>(0);

				for (var entry : clock.events().entrySet()) {
					if (entry.getKey().equals("finished")) {
						continue;
					}

					if (entry.getValue().isEmpty()) {
						allEvents.add(entry.getKey());
					} else {
						for (var t : entry.getValue()) {
							events.computeIfAbsent(t, k -> new ArrayList<>(1)).add(entry.getKey());
						}
					}
				}
			}

			var eventsNow = events.get(tick);

			if (eventsNow != null && !eventsNow.isEmpty()) {
				for (var event : eventsNow) {
					NeoForge.EVENT_BUS.post(new ClockEvent(level, clock, tick, event));
				}
			}

			if (!allEvents.isEmpty()) {
				for (var event : allEvents) {
					NeoForge.EVENT_BUS.post(new ClockEvent(level, clock, tick, event));
				}
			}

			if (tick == clock.maxTicks()) {
				NeoForge.EVENT_BUS.post(new ClockEvent(level, clock, tick, "finished"));
			}
		}
	}

	public void start(MinecraftServer server) {
		this.ticking = true;
		sync(server);
	}

	public void stop(MinecraftServer server) {
		this.ticking = false;
		sync(server);
	}

	public void reset(MinecraftServer server) {
		this.tick = 0;
		this.ticking = false;
		sync(server);
	}

	public void restart(MinecraftServer server) {
		this.tick = 0;
		this.ticking = true;
		sync(server);
	}

	public void setTick(MinecraftServer server, int tick) {
		this.tick = tick;
		sync(server);
	}

	public void sync(MinecraftServer server) {
		server.s2c(new SyncClockInstancePayload(clock.id(), tick, ticking));
	}
}
