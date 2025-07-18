package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.util.Cast;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Props<L extends Level> {
	public final L level;
	public final Map<PropListType, PropList> propLists;
	public final PropList levelProps;
	public final PropList dataProps;

	public Props(L level) {
		this.level = level;
		this.propLists = new EnumMap<>(PropListType.class);
		this.propLists.put(PropListType.LEVEL, levelProps = new PropList(this, PropListType.LEVEL));
		this.propLists.put(PropListType.DATA, dataProps = new PropList(this, PropListType.DATA));
	}

	public <P extends Prop> PropContext<P> context(PropType<P> type, PropSpawnType spawnType, long createdTime) {
		return new PropContext<>(this, type, spawnType, createdTime);
	}

	public void tick() {
		for (var list : propLists.values()) {
			list.tick(null);
		}
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
			propLists.get(prop.spawnType.listType).pending.add(prop);
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

					if (p.isRequired() && p.get(Cast.to(prop)) == null) {
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
		var result = create(context(type, level.isClientSide ? PropSpawnType.ASSETS : PropSpawnType.GAME, level.getGameTime()), true, true, null, null, onCreated);
		return result.isSuccess() ? result.getOrThrow() : null;
	}

	@Nullable
	public <P extends Prop> P addDelayed(PropType<P> type, int delay, @Nullable Consumer<P> onCreated) {
		var result = create(context(type, level.isClientSide ? PropSpawnType.ASSETS : PropSpawnType.GAME, level.getGameTime() + delay), false, true, null, null, onCreated);

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
}
