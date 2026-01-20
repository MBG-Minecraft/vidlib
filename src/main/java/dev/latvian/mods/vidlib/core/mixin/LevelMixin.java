package dev.latvian.mods.vidlib.core.mixin;

import com.google.gson.JsonElement;
import dev.latvian.mods.vidlib.core.VLLevel;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModificationHolder;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.util.PauseType;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Level.class)
public abstract class LevelMixin implements VLLevel {
	@Unique
	private final List<UndoableModificationHolder> vl$undoable = new ArrayList<>();

	@Unique
	private List<LivingEntity> vl$bosses = List.of();

	@Unique
	private LivingEntity vl$mainBoss = null;

	@Unique
	private KNumberContext vl$knumberContext = null;

	@Unique
	private RegistryOps<Tag> vl$nbtOps = null;

	@Unique
	private RegistryOps<JsonElement> vl$jsonOps = null;

	@Unique
	private TagParser<Tag> vl$nbtParser = null;

	@Inject(method = "tickBlockEntities", at = @At("RETURN"))
	private void vl$tickProps(CallbackInfo ci) {
		getProps().tick(getEnvironment().getPauseType().tick());
	}

	@Override
	public void vl$preTick(PauseType paused) {
		vl$bosses = new ArrayList<>(1);
		vl$mainBoss = null;

		for (var entity : allEntities()) {
			if (entity instanceof LivingEntity l && l.isBoss() && l.isAlive()) {
				vl$bosses.add(l);
				vl$mainBoss = l;
			}
		}

		getGlobalContext().updateLevelData(vl$level());
	}

	@Override
	public List<UndoableModificationHolder> vl$getUndoableModifications() {
		return vl$undoable;
	}

	@Override
	public List<LivingEntity> getBosses() {
		return vl$bosses;
	}

	@Override
	@Nullable
	public LivingEntity getMainBoss() {
		return vl$mainBoss;
	}

	@Override
	public KNumberContext getGlobalContext() {
		if (vl$knumberContext == null) {
			vl$knumberContext = VLLevel.super.getGlobalContext();
		}

		return vl$knumberContext;
	}

	@Override
	public RegistryOps<Tag> nbtOps() {
		if (vl$nbtOps == null) {
			vl$nbtOps = VLLevel.super.nbtOps();
		}

		return vl$nbtOps;
	}

	@Override
	public RegistryOps<JsonElement> jsonOps() {
		if (vl$jsonOps == null) {
			vl$jsonOps = VLLevel.super.jsonOps();
		}

		return vl$jsonOps;
	}

	@Override
	public TagParser<Tag> nbtParser() {
		if (vl$nbtParser == null) {
			vl$nbtParser = VLLevel.super.nbtParser();
		}

		return vl$nbtParser;
	}
}
