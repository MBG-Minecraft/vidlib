package dev.latvian.mods.vidlib.feature.ffmpeg;

import dev.latvian.mods.common.CommonPaths;
import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record FFMPEGBinaries(String ffmpeg, String ffprobe, String ffplay) {
	public static final Lazy<Path> FFMPEG_DIRECTORY = CommonPaths.USER.<Path>map(p -> p.resolve("ffmpeg"));
	public static final Lazy<File> FFMPEG_LOG_FILE = Lazy.of(() -> PlatformHelper.CURRENT.getGameDirectory().resolve("ffmpeg.log").toFile());
	private static final FFMPEGBinaries UNKNOWN = new FFMPEGBinaries("", "", "");
	public static final FFMPEGBinaries SYSTEM = new FFMPEGBinaries("ffmpeg", "ffprobe", "ffplay");
	public static FFMPEGBinaries INSTANCE = UNKNOWN;

	public static void initialize() {
		if (INSTANCE != UNKNOWN) {
			return;
		}

		INSTANCE = new FFMPEGBinaries("", "", "");

		CompletableFuture.runAsync(() -> {
			try {
				var binaries = initializeSync();

				if (binaries.isInvalid()) {
					VidLib.LOGGER.error("Failed to initialize FFMPEG");
					return;
				}

				var ffmpeg = FFMPEGProcess.text(List.of(binaries.ffmpeg, "-version")).getFirst();
				var ffprobe = FFMPEGProcess.text(List.of(binaries.ffprobe, "-version")).getFirst();
				var ffplay = FFMPEGProcess.text(List.of(binaries.ffplay, "-version")).getFirst();
				INSTANCE = binaries;
				VidLib.LOGGER.info("FFMPEG initialized successfully. Version info:");
				VidLib.LOGGER.info(ffmpeg);
				VidLib.LOGGER.info(ffprobe);
				VidLib.LOGGER.info(ffplay);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to initialize FFMPEG", ex);
			}
		});
	}

	public static FFMPEGBinaries initializeSync() throws Exception {
		var arm = SystemUtils.OS_ARCH.startsWith("arm") || SystemUtils.OS_ARCH.startsWith("aarch64");

		String version;
		var zipUrls = new ArrayList<String>(1);
		String prefix = "/";

		if (SystemUtils.IS_OS_WINDOWS) {
			version = "9.0.1";
			zipUrls.add("https://github.com/GyanD/codexffmpeg/releases/download/" + version + "/ffmpeg-" + version + "-essentials_build.zip");
			prefix = "/ffmpeg-" + version + "-essentials_build/bin/";
		} else if (SystemUtils.IS_OS_MAC) {
			if (arm) {
				version = "9.0";
				zipUrls.add("https://www.osxexperts.net/ffmpeg9arm.zip");
				zipUrls.add("https://www.osxexperts.net/ffprobe9arm.zip");
				zipUrls.add("https://www.osxexperts.net/ffplay9arm.zip");
			} else {
				version = "9.0.1";
				zipUrls.add("https://evermeet.cx/ffmpeg/ffmpeg-" + version + ".zip");
				zipUrls.add("https://evermeet.cx/ffmpeg/ffprobe-" + version + ".zip");
				zipUrls.add("https://evermeet.cx/ffmpeg/ffplay-" + version + ".zip");
			}
		} else if (SystemUtils.IS_OS_LINUX) {
			version = "8.1.2";

			if (arm) {
				zipUrls.add("https://github.com/Tyrrrz/FFmpegBin/releases/download/" + version + "/ffmpeg-linux-arm64.zip");
			} else {
				zipUrls.add("https://github.com/Tyrrrz/FFmpegBin/releases/download/" + version + "/ffmpeg-linux-x64.zip");
			}
		} else {
			VidLib.LOGGER.error("FFMPEG cannot be downloaded on this system: " + SystemUtils.OS_NAME + ", " + SystemUtils.OS_ARCH);
			return SYSTEM;
		}

		var directory = FFMPEG_DIRECTORY.get().resolve(version);

		if (Files.notExists(directory)) {
			Files.createDirectories(directory);
		}

		var binaries = new FFMPEGBinaries(
			findExecutablePath(directory, "ffmpeg"),
			findExecutablePath(directory, "ffprobe"),
			findExecutablePath(directory, "ffplay")
		);

		if (!binaries.isInvalid()) {
			return binaries;
		}

		try (var client = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.ALWAYS)
			.connectTimeout(Duration.ofMinutes(5L))
			.build()
		) {
			var zipFile = directory.resolve("download.zip");
			VidLib.LOGGER.info("Download path: " + zipFile.toAbsolutePath());

			for (var zipUrl : zipUrls) {
				VidLib.LOGGER.info("Downloading " + zipUrl + "...");

				try (var stream = client.send(HttpRequest.newBuilder(URI.create(zipUrl))
					.GET()
					.timeout(Duration.ofMinutes(5L))
					.build(), HttpResponse.BodyHandlers.ofInputStream()).body()
				) {
					Files.copy(stream, zipFile, StandardCopyOption.REPLACE_EXISTING);
				}

				VidLib.LOGGER.info("Extracting zip " + zipFile.toAbsolutePath() + " -> " + directory.toAbsolutePath());

				try (var fs = IOUtils.openAsZip(zipFile)) {
					var root = fs.getPath("/");

					try (var stream = Files.walk(root)) {
						for (var file : stream.filter(Files::isRegularFile).toList()) {
							var path = file.toString();

							if (path.startsWith(prefix)) {
								VidLib.LOGGER.info("Extracting " + path + "...");
								var output = CommonPaths.mkdirs(directory.resolve(path.substring(prefix.length())));
								Files.copy(file, output, StandardCopyOption.REPLACE_EXISTING);
							}
						}
					}
				}
			}

			VidLib.LOGGER.info("Cleaning up...");
			Files.deleteIfExists(zipFile);
			VidLib.LOGGER.info("All done!");

			return new FFMPEGBinaries(
				findExecutablePath(directory, "ffmpeg"),
				findExecutablePath(directory, "ffprobe"),
				findExecutablePath(directory, "ffplay")
			);
		}
	}

	static String findExecutablePath(Path directory, String program) {
		var fileName = SystemUtils.IS_OS_WINDOWS ? (program + ".exe") : program;

		var directPath = directory.resolve(fileName);

		if (Files.exists(directPath)) {
			return directPath.toAbsolutePath().toString();
		}

		try (var finder = Files.find(directory, 5, (path, attributes) -> path.getFileName().toString().equals(fileName))) {
			return finder.findFirst().map(path -> path.toAbsolutePath().toString()).orElse("");
		} catch (Exception e) {
			VidLib.LOGGER.warn("Failed to locate {}. Using {} on path if it exists.", fileName, program);
			return "";
		}
	}

	public boolean isInvalid() {
		return ffmpeg.isEmpty() || ffprobe.isEmpty() || ffplay.isEmpty();
	}
}
