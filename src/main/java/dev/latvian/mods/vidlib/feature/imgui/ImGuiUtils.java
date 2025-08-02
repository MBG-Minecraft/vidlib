package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImVec2;
import imgui.ImVec4;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;

public interface ImGuiUtils {
	ImInt INT = new ImInt();
	int[] INT2 = new int[2];
	int[] INT3 = new int[3];
	int[] INT4 = new int[4];
	ImFloat FLOAT = new ImFloat();
	float[] FLOAT2 = new float[2];
	float[] FLOAT3 = new float[3];
	float[] FLOAT4 = new float[4];
	ImVec2 VEC2 = new ImVec2();
	ImVec4 VEC4 = new ImVec4();
	ImDouble DOUBLE = new ImDouble();
	double[] DOUBLE2 = new double[2];
	double[] DOUBLE3 = new double[3];
	double[] DOUBLE4 = new double[4];
	ImString STRING = resizableString();
	ImBoolean BOOLEAN = new ImBoolean();

	static ImString resizableString() {
		var s = new ImString("");
		s.inputData.isResizable = true;
		return s;
	}

	static String id(ResourceLocation id) {
		return id.getNamespace() + "-" + id.getPath().replace('/', '-');
	}

	static float getDpiScale() {
		return ImGuiHooks.dpiScale;
	}

	static int getDockId() {
		return ImGuiHooks.dockId;
	}
}
