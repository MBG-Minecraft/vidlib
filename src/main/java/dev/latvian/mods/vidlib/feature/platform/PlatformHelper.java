package dev.latvian.mods.vidlib.feature.platform;

import com.google.gson.JsonObject;
import dev.latvian.mods.klib.util.Side;
import dev.latvian.mods.vidlib.feature.auto.AutoCallback;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
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

	@Nullable
	public Path findFile(String... path) {
		throw new UnsupportedOperationException("Not supported on bukkit");
	}

	@Nullable
	public Path findFile(PackType type, ResourceLocation id) {
		var path = id.getPath().split("/");
		var pathParts = new String[path.length + 2];
		pathParts[0] = type.getDirectory();
		pathParts[1] = id.getNamespace();
		System.arraycopy(path, 0, pathParts, 2, path.length);
		return findFile(pathParts);
	}

	public void collectDynamicResources(PackType type, Consumer<ResourceLocation> callback) {
	}
}
