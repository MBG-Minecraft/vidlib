package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.DataResult;
import dev.latvian.mods.vidlib.util.Empty;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class PropList<L extends Level> {
	public final L level;
	public final Int2ObjectMap<Prop> active;

	public PropList(L level) {
		this.level = level;
		this.active = new Int2ObjectOpenHashMap<>();
	}

	public void tick() {
		if (!active.isEmpty()) {
			active.values().removeIf(Prop::fullTick);
		}
	}

	protected void onAdded(Prop prop) {
	}

	protected void onRemoved(Prop prop) {
	}

	public void add(Prop prop) {
		if (prop.spawnType == PropSpawnType.DUMMY) {
			return;
		}

		prop.level = level;
		active.put(prop.id, prop);
		prop.onAdded();
		onAdded(prop);
		prop.snap();
	}

	public <P extends Prop> DataResult<P> create(PropContext<P> ctx, boolean full, @Nullable BiConsumer<PropList<?>, P> onCreated) {
		var prop = ctx.type().factory().create(ctx);
		var result = ctx.type().load(prop, ctx.propList().level.nbtOps(), ctx.initialData() == null ? Empty.COMPOUND_TAG : ctx.initialData(), full);

		if (onCreated != null && result.isSuccess()) {
			onCreated.accept(this, result.getOrThrow());
		}

		return result;
	}

	@Nullable
	public <P extends Prop> P create(PropType<P> type) {
		var result = create(new PropContext<>(this, type), true, null);
		return result.isSuccess() ? result.getOrThrow() : null;
	}

	@Nullable
	public <P extends Prop> P add(PropType<P> type, Consumer<P> onCreated) {
		var result = create(new PropContext<>(this, type), true, null);

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
		var result = create(new PropContext<>(this, type, PropSpawnType.DUMMY, null), true, null);

		if (result.isSuccess()) {
			var prop = result.getOrThrow();
			onCreated.accept(prop);
			return prop;
		}

		return null;
	}
}
