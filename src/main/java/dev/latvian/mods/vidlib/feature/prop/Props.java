package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.DataResult;
import dev.latvian.mods.vidlib.util.Empty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class Props<L extends Level> {
	public final L level;
	public final Map<PropListType, PropList> propLists;

	public Props(L level) {
		this.level = level;
		this.propLists = new EnumMap<>(PropListType.class);
		this.propLists.put(PropListType.LEVEL, new PropList(this, PropListType.LEVEL));
		this.propLists.put(PropListType.DATA, new PropList(this, PropListType.DATA));
	}

	public <P extends Prop> PropContext<P> context(PropType<P> type, PropSpawnType spawnType, long createdTime, @Nullable CompoundTag initialData) {
		return new PropContext<>(this, type, spawnType, createdTime, initialData);
	}

	public <P extends Prop> PropContext<P> context(PropType<P> type, long spawnedGameTime) {
		return context(type, PropSpawnType.GAME, spawnedGameTime, null);
	}

	public void tick() {
		for (var list : propLists.values()) {
			list.tick();
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
		if (!isValid(prop.spawnType)) {
			return;
		}

		prop.level = level;
		propLists.get(prop.spawnType.listType).add(prop);
		prop.onAdded();
		onAdded(prop);
		prop.snap();
	}

	public <P extends Prop> DataResult<P> create(PropContext<P> ctx, boolean full, @Nullable BiConsumer<Props<?>, P> onCreated) {
		var prop = ctx.type().factory().create(ctx);
		var result = ctx.type().load(prop, ctx.props().level.nbtOps(), ctx.initialData() == null ? Empty.COMPOUND_TAG : ctx.initialData(), full);

		if (onCreated != null && result.isSuccess()) {
			onCreated.accept(this, result.getOrThrow());
		}

		return result;
	}

	@Nullable
	public <P extends Prop> P create(PropType<P> type) {
		var result = create(context(type, level.getGameTime()), true, null);
		return result.isSuccess() ? result.getOrThrow() : null;
	}

	@Nullable
	public <P extends Prop> P add(PropType<P> type, Consumer<P> onCreated) {
		var result = create(context(type, level.getGameTime()), true, null);

		if (result.isSuccess()) {
			var prop = result.getOrThrow();
			onCreated.accept(prop);
			add(prop);
			return prop;
		}

		return null;
	}

	@Nullable
	public <P extends Prop> P createDummy(PropType<P> type, Consumer<P> onCreated) {
		var result = create(context(type, PropSpawnType.DUMMY, level.getGameTime(), null), true, null);

		if (result.isSuccess()) {
			var prop = result.getOrThrow();
			onCreated.accept(prop);
			return prop;
		}

		return null;
	}
}
