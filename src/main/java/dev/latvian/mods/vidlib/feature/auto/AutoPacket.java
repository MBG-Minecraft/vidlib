package dev.latvian.mods.vidlib.feature.auto;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoPacket {
	enum Stage {
		COMMON("common", List.of(ConnectionProtocol.PLAY, ConnectionProtocol.CONFIGURATION)),
		CONFIG("config", List.of(ConnectionProtocol.CONFIGURATION)),
		GAME("game", List.of(ConnectionProtocol.PLAY));

		public static final EnumSet<Stage> DEFAULT = EnumSet.of(GAME);

		public final String string;
		public final List<ConnectionProtocol> protocols;

		Stage(String string, List<ConnectionProtocol> protocols) {
			this.string = string;
			this.protocols = protocols;
		}

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	enum To {
		CLIENT("s2c", Optional.of(PacketFlow.CLIENTBOUND)),
		SERVER("c2s", Optional.of(PacketFlow.SERVERBOUND)),
		BIDI("bidi", Optional.empty());

		public static final EnumSet<To> DEFAULT = EnumSet.of(CLIENT);

		public final String string;
		public final Optional<PacketFlow> flow;

		To(String string, Optional<PacketFlow> flow) {
			this.string = string;
			this.flow = flow;
		}

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	Stage stage() default Stage.GAME;

	To to() default To.CLIENT;

	record ScanData(String className, VidLibPacketType<?> type, Stage stage, To to) {
	}

	@ApiStatus.Internal
	Lazy<List<ScanData>> SCANNED = Lazy.of(() -> {
		var list = new ArrayList<ScanData>();
		VidLib.LOGGER.info("Scanning @AutoPacket...");

		AutoHelper.load(AutoPacket.class, EnumSet.of(ElementType.FIELD), (source, classLoader, ad) -> {
			var clazz = AutoHelper.initClass(ad, classLoader);
			var type = AutoHelper.getStaticFieldValue(clazz, ad);

			if (type instanceof VidLibPacketType<?> t) {
				var stage = AutoHelper.getEnumValue(ad, Stage.class, "stage", Stage.GAME);
				var to = AutoHelper.getEnumValue(ad, To.class, "to", To.CLIENT);
				list.add(new ScanData(clazz.getName(), t, stage, to));
			}
		});

		return list;
	});
}
