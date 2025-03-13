package dev.beast.mods.shimmer.feature.worldsync;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ClientCommandHolder;
import dev.beast.mods.shimmer.util.MessageConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class WorldSyncCommands {
	@AutoRegister(Dist.CLIENT)
	public static final ClientCommandHolder HOLDER = new ClientCommandHolder("world-sync", (command, buildContext) -> command
		.then(Commands.literal("pull")
			.executes(ctx -> pull())
		)
		.then(Commands.literal("create")
			.then(Commands.argument("name", StringArgumentType.word())
				.executes(ctx -> create(ctx.getSource(), StringArgumentType.getString(ctx, "name"), ""))
				.then(Commands.argument("display-name", StringArgumentType.word())
					.executes(ctx -> create(ctx.getSource(), StringArgumentType.getString(ctx, "name"), StringArgumentType.getString(ctx, "display-name")))
				)
			)
		)
	);

	public static int pull() {
		Minecraft.getInstance().c2s(PullWorldSyncPayload.INSTANCE);
		return 1;
	}

	public static int create(CommandSourceStack source, String name, String displayName) {
		try {
			return create(MessageConsumer.ofCommandSource(source), name, displayName.isEmpty() ? name : displayName);
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	public static int create(MessageConsumer messageConsumer, String name, String displayName) throws Exception {
		var localPath = Shimmer.WORLD_SYNC_PATH.get().resolve(name);
		var worldIndex = WorldIndex.load(localPath);

		if (Files.notExists(localPath) || !worldIndex.found()) {
			messageConsumer.error(Component.literal("Synced world '" + name + "' not found!"));
			return 0;
		}

		var worldPath = FMLPaths.GAMEDIR.get().resolve("saves/" + name);

		if (Files.exists(worldPath)) {
			Files.walkFileTree(worldPath, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
			});
		}

		Files.createDirectory(worldPath);

		for (var file : worldIndex.files().values()) {
			var path = worldPath.resolve(file.path());
			var pathParent = path.getParent();

			if (Files.notExists(pathParent)) {
				Files.createDirectories(pathParent);
			}

			if (file.size() == 0L) {
				Files.createFile(path);
			} else if (Files.exists(file.file())) {
				Files.copy(file.file(), path);
			} else {
				messageConsumer.error(Component.literal("Couldn't find " + file.checksum() + " - " + file.path() + " file!"));
			}
		}

		var levelDatPath = worldPath.resolve("level.dat");

		if (Files.exists(levelDatPath)) {
			try {
				var levelDat = NbtIo.readCompressed(levelDatPath, NbtAccounter.unlimitedHeap());
				var data = levelDat.getCompound("Data");
				data.putBoolean("allowCommands", true);
				data.putString("LevelName", displayName);
				NbtIo.writeCompressed(levelDat, levelDatPath);
			} catch (Exception ex) {
				Shimmer.LOGGER.error("[World Sync] Failed to update level.dat", ex);
			}
		}

		messageConsumer.success(Component.literal("Created world '" + displayName + "'!").withStyle(ChatFormatting.GREEN));
		return 1;
	}
}
