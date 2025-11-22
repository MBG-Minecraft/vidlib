package dev.latvian.mods.vidlib.feature.platform;

import com.google.gson.JsonObject;
import dev.latvian.mods.klib.util.Side;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoCallback;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;

public class PlatformHelper {
	public static PlatformHelper CURRENT = new PlatformHelper();

	public Side getSide() {
		return Side.SERVER;
	}

	public String getPlatform() {
		return "bukkit";
	}

	public RegistryFriendlyByteBuf createBuffer(ByteBuf source, RegistryAccess access) {
		return new RegistryFriendlyByteBuf(source, access);
	}

	public RegistryFriendlyByteBuf createBuffer(ByteBuf source, RegistryFriendlyByteBuf parent) {
		return new RegistryFriendlyByteBuf(source, parent.registryAccess());
	}

	public Function<ByteBuf, RegistryFriendlyByteBuf> createDecorator(RegistryAccess access) {
		return RegistryFriendlyByteBuf.decorator(access);
	}

	public void load(Class<? extends Annotation> annotation, Set<ElementType> elementTypes, AutoCallback callback) {
	}

	public void finishPacketCapture(PacketCapture packetCapture) {
	}

	public void packetCaptureMetadata(PacketCapture packetCapture, JsonObject metadata) {
	}

	public Path findFile(String modid, String... path) {
		throw new UnsupportedOperationException("Not supported on bukkit");
	}

	public Path findVidLibFile(String... path) {
		return findFile(VidLib.ID, path);
	}
}
