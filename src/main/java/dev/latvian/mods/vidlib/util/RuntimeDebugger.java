package dev.latvian.mods.vidlib.util;

import java.util.Properties;

public class RuntimeDebugger {
	private static int jdwpListenerPort = 0;

	public static int getJdwpListenerPort() {
		if (jdwpListenerPort == 0) {
			jdwpListenerPort = readJdwpListenerPort();
		}

		return jdwpListenerPort;
	}

	private static Class<?> vmSupportClass() {
		try {
			return Class.forName("jdk.internal.vm.VMSupport");
		} catch (Exception ignored) {
			try {
				return Class.forName("sun.misc.VMSupport");
			} catch (Exception ignored2) {
			}
		}

		return null;
	}

	private static int readJdwpListenerPort() {
		try {
			var vmSupportClass = vmSupportClass();

			if (vmSupportClass == null) {
				return -1;
			}

			var m = vmSupportClass.getMethod("getAgentProperties");
			var p = (Properties) m.invoke(null);
			var listenerAddress = p.getProperty("sun.jdwp.listenerAddress");

			if (listenerAddress != null) {
				return Integer.parseInt(listenerAddress.substring(listenerAddress.lastIndexOf(':') + 1));
			}
		} catch (Exception ignored) {
		}

		return -1;
	}
}
