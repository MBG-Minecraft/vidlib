package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Screen.class)
public abstract class ScreenMixin implements VLScreen {
}
