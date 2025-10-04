#version 410 core

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D DataSampler;
uniform int Count;
uniform float GameTime;
uniform mat4 InverseViewProjectionMat;

in vec2 texCoord;
in vec2 oneTexel;

layout (location = 0) out vec4 fragColor;

uvec4 fetchRGBA8u(ivec2 tc) {
	vec4 s = texelFetch(DataSampler, tc, 0) * 255.0;
	return uvec4(round(s));
}

uint decodeUInt(int x, int y) {
	uvec4 v = fetchRGBA8u(ivec2(x, y));
	return (v.w << 24) | (v.x << 16) | (v.y << 8) | v.z;
}

float decodeFloat(int x, int y) {
	return uintBitsToFloat(decodeUInt(x, y));
}

vec4 decodeColor(int x, int y) {
	return texelFetch(DataSampler, ivec2(x, y), 0);
}

vec4 blend(vec4 src, vec4 dst) {
	vec3 c = src.rgb * src.a + dst.rgb * (1.0 - src.a);
	float a = src.a + dst.a * (1.0 - src.a);
	return vec4(c, a);
}

vec3 clip(float depth) {
	vec4 clipPos;
	clipPos.xy = texCoord * 2.0 - 1.0;
	clipPos.z = depth * 2.0 - 1.0;
	clipPos.w = 1.0;
	vec4 homogenousPos = InverseViewProjectionMat * clipPos;
	return homogenousPos.xyz / homogenousPos.w;
}

void main() {
	if (Count == 0) {
		discard;
	}

	float depth = texture(InDepthSampler, texCoord).r;
	vec3 worldPos = clip(depth);

	vec4 color = vec4(texture(InSampler, texCoord).rgb, 1.0);
	bool modified = false;

	for (int y = 0; y < 1024; y++) {
		if (y >= Count) {
			break;
		}

		int head = int(decodeUInt(0, y));
		int type = head & 15;

		if (type == 0) {
			continue;
		}

		if (type == 1) {
			// float start = decodeFloat(1, y);
		}

		// color = blend(color, decalColor);
		// modified = true;
	}

	if (modified) {
		fragColor = color;
	} else {
		discard;
	}
}
