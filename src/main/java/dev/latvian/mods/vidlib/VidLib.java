package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.entity.PlayerTarget;
import dev.latvian.mods.klib.net.S2CPacketBundleBuilder;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.registry.CustomRegistryCollector;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.replay.api.ReplayMarkerGroup;
import dev.latvian.mods.vidlib.client.VidLibClient;
import dev.latvian.mods.vidlib.core.VLExtensions;
import dev.latvian.mods.vidlib.feature.atmosphere.VidLibEnvironmentAttributes;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.cutscene.step.CutsceneStepType;
import dev.latvian.mods.vidlib.feature.ffmpeg.FFMPEGBinaries;
import dev.latvian.mods.vidlib.feature.font.TTFFile;
import dev.latvian.mods.vidlib.feature.misc.ReplayMarkerPayload;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticles;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import dev.mrbeastgaming.mods.hub.HubConfig;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubServerSession;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VidLib {
	public static final String ID = "vidlib";
	public static final String NAME = "VidLib";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
	public static String VERSION = "unknown";

	private static boolean initialized = false;
	private static boolean gameLoaded = false;
	private static MinecraftServer initializedServer = null;

	public static synchronized void init() {
		if (initialized) {
			return;
		}

		initialized = true;

		var implementationVersion = VidLib.class.getPackage().getImplementationVersion();

		if (VERSION.equals("unknown") && implementationVersion != null && !implementationVersion.isEmpty()) {
			VERSION = implementationVersion;
		}

		VidLib.LOGGER.info("VidLib " + VERSION + " loaded");
		Runtime.getRuntime().addShutdownHook(new Thread(HubServerGateway::stopGateway, "Stop-Server-Hub-Gateway"));

		if (PlatformHelper.INSTANCE.isDevEnv()) {
			FFMPEGBinaries.initialize();
		}

		HubConfig.load();
		VidLibEnvironmentAttributes.init();
		VidLibParticles.init();
	}

	public static synchronized void gameLoaded() {
		if (!gameLoaded) {
			gameLoaded = true;
			AutoInit.Type.GAME_LOADED.invoke();
		}
	}

	public static synchronized void initServer(MinecraftServer server) {
		if (initializedServer == server) {
			return;
		}

		initializedServer = server;
		VLExtensions.init();
		init();
		LOGGER.info("Netty Threads: " + Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2)));
		gameLoaded();
		HubServerSession.loadAsync(server);
		CommonGameEngine.INSTANCE.setupServer(server);
		AutoInit.Type.SERVER_STARTED.invoke(server);
	}

	public static void initClient() {
		VidLibClient.init();
	}

	public static void builtInRegistries(CustomRegistryCollector registry) {
		var platform = VLPlatformHelper.INSTANCE;
		registry.register(ScreenShakeType.REGISTRY, platform::collectScreenShakeTypes);
		registry.register(ScreenEffect.REGISTRY, platform::collectScreenEffectTypes);
		registry.register(PhysicsParticleData.REGISTRY);
		registry.register(CutsceneStepType.REGISTRY, platform::collectCutsceneStepTypes);
		registry.register(Cutscene.REGISTRY);
		registry.register(TTFFile.REGISTRY, platform::collectTTFFiles);
	}

	public static void sync(ServerPlayer player, int syncType) {
		var session = PlayerSessionData.of(player);

		if (session.globalData.isReplay) {
			return;
		}

		var packets = new S2CPacketBundleBuilder();
		packets.s2c(new ReplayMarkerPayload(ReplayMarkerData.builder().group(ReplayMarkerGroup.DATA_SYNC).build()));
		session.sync(packets, player, syncType);
		packets.send(PlayerTarget.of(player));
	}

	public static void errorToHub(String message, Throwable ex) {
		LOGGER.error(message, ex);

		var gateway = HubAPI.getClientOrServerGateway();

		if (gateway != null) {
			gateway.log(0, null, message, ex);
		}
	}
}
