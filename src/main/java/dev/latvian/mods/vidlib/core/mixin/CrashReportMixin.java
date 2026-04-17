package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.mrbeastgaming.mods.hub.api.HubFileType;
import dev.mrbeastgaming.mods.hub.file.ClientHubFileUploads;
import dev.mrbeastgaming.mods.hub.file.ServerHubFileUploads;
import net.minecraft.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;
import java.nio.file.Path;

@Mixin(CrashReport.class)
public class CrashReportMixin {
	@Shadow
	@Nullable
	private Path saveFile;

	@ModifyReturnValue(method = "saveToFile(Ljava/nio/file/Path;Lnet/minecraft/ReportType;Ljava/util/List;)Z", at = @At("RETURN"))
	private boolean vl$saveToFile(boolean original) {
		if (saveFile != null) {
			if (PlatformHelper.CURRENT.getSide().isClient()) {
				ClientHubFileUploads.syncFile(saveFile, builder -> {
					builder.setType(HubFileType.CRASH_REPORT);
					builder.setFilterEndsWith("-client.txt");
				});
			} else {
				ServerHubFileUploads.syncFile(saveFile, builder -> {
					builder.setType(HubFileType.CRASH_REPORT);
					builder.setFilterEndsWith("-server.txt");
				});
			}
		}

		return original;
	}
}
