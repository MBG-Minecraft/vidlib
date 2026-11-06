package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.net.S2CPacketBundleBuilder;
import dev.latvian.mods.klib.net.S2CPacketConsumer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class Props<L extends Level> {
	public final L level;
	public final PropList levelProps;

	public Props(L level) {
		this.level = level;
		levelProps = new PropList(this);
	}

	public <P extends Prop> PropContext<P> context(PropType<P> type, PropSpawnType spawnType, long createdTime) {
		return new PropContext<>(this, type, spawnType, createdTime);
	}

	public void tick(boolean tick) {
		levelProps.tick(null, tick);
	}

	public void tickBukkit(@Nullable S2CPacketConsumer updates, boolean tick) {
		levelProps.tickBukkit(null, tick);
	}

	protected void onAdded(Prop prop) {
	}

	protected void onRemoved(Prop prop) {
	}

	protected boolean isValid(PropSpawnType type) {
		return type != PropSpawnType.DUMMY;
	}

	public void add(Prop prop) {
		if (isValid(prop.spawnType)) {
			if (levelProps.queueNewProps || level.isClientSide()) {
				levelProps.add(prop, null);
			} else {
				var builder = new S2CPacketBundleBuilder();
				levelProps.add(prop, builder);
				builder.send(level);
			}
		}
	}

	public <O, P extends Prop> DataResult<P> create(PropContext<P> ctx, boolean add, boolean validate, @Nullable DynamicOps<O> ops, @Nullable O initialData, @Nullable Consumer<P> onCreated) {
		var prop = ctx.type().factory().create(ctx);

		if (ops != null && initialData != null) {
			var result = ctx.type().load(prop, ops, initialData, validate);

			if (result.isError()) {
				return result;
			}
		}

		if (onCreated != null) {
			onCreated.accept(prop);

			if (validate && (ops == null || initialData == null)) {
				for (var entry : prop.type.data()) {
					var p = entry.data();

					if (p.isRequired() && prop.getData(p) == null) {
						return DataResult.error(() -> "Missing required data key '" + p.key() + "'");
					}
				}
			}

			if (add) {
				add(prop);
			}
		}

		return DataResult.success(prop);
	}

	@Nullable
	public <P extends Prop> P add(PropType<P> type, @Nullable Consumer<P> onCreated) {
		var result = create(context(type, PropSpawnType.GAME, level.getGameTime()), true, true, null, null, onCreated);
		return result.isSuccess() ? result.getOrThrow() : null;
	}

	@Nullable
	public <P extends Prop> P addDelayed(PropType<P> type, int delay, @Nullable Consumer<P> onCreated) {
		var result = create(context(type, PropSpawnType.GAME, level.getGameTime() + delay), false, true, null, null, onCreated);

		if (result.isSuccess()) {
			var prop = result.getOrThrow();
			level.getEnvironment().schedule(delay, () -> add(prop));
			return prop;
		}

		return null;
	}

	@Nullable
	public <P extends Prop> P createDummy(PropType<P> type, @Nullable Consumer<P> onCreated) {
		var result = create(context(type, PropSpawnType.DUMMY, level.getGameTime()), false, false, null, null, onCreated);
		return result.isSuccess() ? result.getOrThrow() : null;
	}

	@Nullable
	public PropHitResult clip(ClipContext ctx, boolean ignoreInteractable) {
		PropHitResult result = null;

		var propClip = levelProps.clip(ctx, ignoreInteractable);

		if (propClip != null) {
			if (result == null || propClip.getLocation().distanceToSqr(ctx.getFrom()) < result.getLocation().distanceToSqr(ctx.getFrom())) {
				result = propClip;
			}
		}

		return result;
	}
}
