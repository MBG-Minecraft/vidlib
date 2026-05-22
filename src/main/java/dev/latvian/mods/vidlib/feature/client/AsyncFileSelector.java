package dev.latvian.mods.vidlib.feature.client;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AsyncFileSelector {
	@FunctionalInterface
	private interface DialogTask {
		int run(MemoryStack stack, PointerBuffer out) throws Throwable;
	}

	private static final ExecutorService DIALOG_THREAD = Executors.newSingleThreadExecutor(r -> {
		var t = new Thread(r, "File Dialog Thread");
		t.setDaemon(true);
		return t;
	});

	private static CompletableFuture<Path> currentDialog;
	private static boolean nfdInitialized = false;

	private AsyncFileSelector() {
	}

	public static boolean hasDialog() {
		return currentDialog != null;
	}

	private static CompletableFuture<Path> open(DialogTask task) {
		if (hasDialog()) {
			return CompletableFuture.completedFuture(null);
		}

		var future = new CompletableFuture<Path>();
		currentDialog = future;

		var wasInitialized = nfdInitialized;
		nfdInitialized = true;

		Runnable runnable = () -> {
			if (!wasInitialized) {
				NativeFileDialog.NFD_Init();
			}

			Path resultPath = null;

			try (var stack = MemoryStack.stackPush()) {
				var out = stack.callocPointer(1);
				int result = task.run(stack, out);

				if (result == NativeFileDialog.NFD_OKAY) {
					resultPath = Path.of(out.getStringUTF8(0));
					NativeFileDialog.NFD_FreePath(out.get(0));
				}
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				future.complete(resultPath);
				currentDialog = null;
			}
		};

		if (Minecraft.ON_OSX) {
			// MacOS needs dialogs to be run from the main thread
			Minecraft.getInstance().execute(runnable);
		} else {
			DIALOG_THREAD.execute(runnable);
		}

		return future;
	}

	public static CompletableFuture<Path> saveFileDialog(@Nullable Path defaultPath, String defaultName, String filterDescription, String... filters) {
		return open((stack, out) -> {
			var filtersBuffer = buildFilters(stack, filterDescription, filters);
			return NativeFileDialog.NFD_SaveDialog(out, filtersBuffer, filter(defaultPath), filter(defaultName));
		});
	}

	public static CompletableFuture<Path> openFileDialog(@Nullable Path defaultPath, String filterDescription, String... filters) {
		return open((stack, out) -> {
			var filtersBuffer = buildFilters(stack, filterDescription, filters);
			return NativeFileDialog.NFD_OpenDialog(out, filtersBuffer, filter(defaultPath));
		});
	}

	public static CompletableFuture<Path> openDirectoryDialog(@Nullable Path defaultPath) {
		return open((stack, out) -> NativeFileDialog.NFD_PickFolder(out, filter(defaultPath)));
	}

	private static NFDFilterItem.Buffer buildFilters(MemoryStack stack, String description, String... specs) {
		if (specs == null || specs.length == 0) {
			return null;
		}

		var specBuilder = new StringBuilder();

		for (var spec : specs) {
			if (!specBuilder.isEmpty()) {
				specBuilder.append(',');
			}
			specBuilder.append(filter(spec));
		}

		var buffer = NFDFilterItem.malloc(1, stack);
		buffer.get(0)
			.name(stack.UTF8(filter(description)))
			.spec(stack.UTF8(specBuilder.toString()));
		return buffer;
	}

	private static String filter(@Nullable Path path) throws IOException {
		if (path == null) {
			return null;
		}

		return filter(path.toRealPath().toAbsolutePath().toString());
	}

	public static String filter(@Nullable CharSequence in) {
		if (in == null) {
			return null;
		}

		return filterLT20(in.toString()
			.replace("'", "")
			.replace("\"", "")
			.replace("$", "")
			.replace("`", ""));
	}

	private static String filterLT20(@Nullable CharSequence in) {
		if (in == null) {
			return null;
		}

		var s = in.toString();
		var builder = new StringBuilder(s.length());

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 32 || c == '\n' || c == '\t' || c == '\r') {
				builder.append(c);
			}
		}

		return builder.toString();
	}
}
